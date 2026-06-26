package com.jayjaycode.miniproject.data

import android.content.Context
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jayjaycode.miniproject.data.firebase.FirestoreConstants
import com.jayjaycode.miniproject.data.firebase.FirestoreMappers
import com.jayjaycode.miniproject.util.FirebaseStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RescueRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    companion object {
        val instance = RescueRepository()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var bidsListener: ListenerRegistration? = null
    private var requestListener: ListenerRegistration? = null
    private var biddingTimerJob: Job? = null

    private val _activeRequestId = MutableStateFlow<String?>(null)
    val activeRequestId: Flow<String?> = _activeRequestId.asStateFlow()

    private val _activeRequest = MutableStateFlow<BreakdownRequest?>(null)
    val activeRequest: Flow<BreakdownRequest?> = _activeRequest.asStateFlow()

    private var currentBiddingEndsAtMillis: Long = 0L

    private val _bids = MutableStateFlow<List<MechanicBid>>(emptyList())
    val bids: Flow<List<MechanicBid>> = _bids.asStateFlow()

    private val _biddingSecondsLeft = MutableStateFlow(0)
    val biddingSecondsLeft: Flow<Int> = _biddingSecondsLeft.asStateFlow()

    private val _acceptedJob = MutableStateFlow<ActiveJob?>(null)
    val acceptedJob: Flow<ActiveJob?> = _acceptedJob.asStateFlow()

    private val _requestHistory = MutableStateFlow<List<BreakdownRequest>>(emptyList())
    val requestHistory: Flow<List<BreakdownRequest>> = _requestHistory.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: Flow<Boolean> = _isLoadingHistory.asStateFlow()

    val biddingDurationSeconds: Int get() = FirestoreConstants.BIDDING_DURATION_SECONDS

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: error("You must be signed in")

    suspend fun ensureUserProfile(displayName: String, email: String) {
        val uid = requireUserId()
        val doc = firestore.collection(FirestoreConstants.USERS).document(uid)
        val snapshot = doc.get().await()
        if (!snapshot.exists()) {
            doc.set(
                mapOf(
                    "displayName" to displayName,
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
        } else if (displayName.isNotBlank()) {
            doc.update("displayName", displayName).await()
        }
    }

    private suspend fun ensureFreshAuthToken() {
        auth.currentUser?.getIdToken(true)?.await()
    }

    private suspend fun uploadBreakdownPhotos(
        userId: String,
        requestId: String,
        photoUris: List<Uri>,
        context: Context,
    ): List<String> {
        if (photoUris.isEmpty()) return emptyList()
        ensureFreshAuthToken()

        val uploaded = mutableListOf<String>()
        var lastError: Exception? = null
        photoUris.forEachIndexed { index, uri ->
            if (uri.scheme?.startsWith("http") == true) {
                uploaded.add(uri.toString())
                return@forEachIndexed
            }
            runCatching {
                FirebaseStorageHelper.uploadImage(
                    pathSegments = listOf(
                        FirestoreConstants.BREAKDOWN_REQUEST_PHOTOS,
                        userId,
                        requestId,
                        "photo_$index.jpg",
                    ),
                    sourceUri = uri,
                    context = context,
                )
            }.onSuccess { uploaded.add(it) }
                .onFailure { lastError = it as? Exception ?: Exception(it.message, it) }
        }
        if (uploaded.isEmpty() && lastError != null) {
            throw lastError!!
        }
        return uploaded
    }

    suspend fun submitBreakdownRequest(
        type: RequestType,
        vehicle: VehicleInfo,
        problemDescription: String,
        damageDescription: String,
        locationLabel: String,
        latitude: Double,
        longitude: Double,
        photoUris: List<Uri> = emptyList(),
        context: Context,
    ): BreakdownRequest {
        val user = auth.currentUser ?: error("You must be signed in")
        val now = System.currentTimeMillis()
        val biddingEndsAt = now + FirestoreConstants.BIDDING_DURATION_SECONDS * 1000L

        val docRef = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS).document()
        val uploadedPhotoUrls = uploadBreakdownPhotos(
            userId = user.uid,
            requestId = docRef.id,
            photoUris = photoUris,
            context = context,
        )

        val request = BreakdownRequest(
            id = docRef.id,
            type = type,
            vehicle = vehicle,
            problemDescription = problemDescription,
            damageDescription = damageDescription,
            locationLabel = locationLabel,
            latitude = latitude,
            longitude = longitude,
            photoUris = uploadedPhotoUrls,
            status = RequestStatus.BIDDING,
            createdAtMillis = now,
            userId = user.uid,
            biddingEndsAtMillis = biddingEndsAt,
        )

        docRef.set(
            FirestoreMappers.requestToMap(request, user.uid, user.email.orEmpty(), biddingEndsAt),
        ).await()

        clearListeners()
        _activeRequestId.value = docRef.id
        _activeRequest.value = request
        _bids.value = emptyList()
        _acceptedJob.value = null
        _biddingSecondsLeft.value = FirestoreConstants.BIDDING_DURATION_SECONDS

        attachRequestListener(docRef.id)
        attachBidsListener(docRef.id)
        currentBiddingEndsAtMillis = biddingEndsAt
        startBiddingTimer(biddingEndsAt)

        refreshRequestHistory()
        return request
    }

    private fun attachRequestListener(requestId: String) {
        requestListener?.remove()
        requestListener = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val request = FirestoreMappers.requestFromDocument(snapshot) ?: return@addSnapshotListener
                _activeRequest.value = request

                if (request.status == RequestStatus.BIDDING) {
                    val endsAt = request.biddingEndsAtMillis
                    if (endsAt > System.currentTimeMillis() && endsAt != currentBiddingEndsAtMillis) {
                        startBiddingTimer(endsAt)
                    }
                }

                if (request.status == RequestStatus.ACCEPTED || request.status == RequestStatus.IN_PROGRESS) {
                    val acceptedMap = snapshot.get("acceptedBid") as? Map<String, Any>
                    val bid = FirestoreMappers.acceptedBidFromMap(acceptedMap) ?: return@addSnapshotListener
                    scope.launch {
                        val shop = resolveShop(bid)
                        _acceptedJob.value = ActiveJob(request = request, acceptedBid = bid, mechanicShop = shop)
                    }
                    _biddingSecondsLeft.value = 0
                    stopBiddingJobs()
                }

                if (request.status == RequestStatus.CANCELLED) {
                    clearActiveSession()
                }
            }
    }

    private fun attachBidsListener(requestId: String) {
        bidsListener?.remove()
        bidsListener = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .collection(FirestoreConstants.BIDS)
            .orderBy("price", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val bids = snapshot.documents.mapNotNull { FirestoreMappers.bidFromDocument(it) }
                _bids.value = bids
            }
    }

    private fun startBiddingTimer(biddingEndsAtMillis: Long) {
        currentBiddingEndsAtMillis = biddingEndsAtMillis
        biddingTimerJob?.cancel()
        biddingTimerJob = scope.launch {
            while (true) {
                val secondsLeft = ((biddingEndsAtMillis - System.currentTimeMillis()) / 1000).toInt()
                _biddingSecondsLeft.value = secondsLeft.coerceAtLeast(0)
                if (secondsLeft <= 0) {
                    maybeAutoAcceptLowestBid()
                    break
                }
                delay(1000)
            }
            _biddingSecondsLeft.value = 0
        }
    }

    private suspend fun maybeAutoAcceptLowestBid() {
        val request = _activeRequest.value ?: return
        if (request.status != RequestStatus.BIDDING || !request.autoAcceptLowestBid) return
        val lowest = _bids.value.firstOrNull() ?: return
        runCatching { acceptBid(lowest) }
    }

    suspend fun extendBiddingTime(extraSeconds: Int = FirestoreConstants.BIDDING_EXTENSION_SECONDS) {
        val requestId = _activeRequestId.value ?: error("No active request")
        val request = _activeRequest.value ?: error("No active request")
        if (request.status != RequestStatus.BIDDING) error("Bidding has ended")
        val newEndsAt = maxOf(
            currentBiddingEndsAtMillis,
            System.currentTimeMillis(),
        ) + extraSeconds * 1000L
        firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .update("biddingEndsAt", Timestamp(java.util.Date(newEndsAt)))
            .await()
        _activeRequest.value = request.copy(biddingEndsAtMillis = newEndsAt)
        startBiddingTimer(newEndsAt)
    }

    suspend fun setAutoAcceptLowestBid(enabled: Boolean) {
        val requestId = _activeRequestId.value ?: error("No active request")
        val request = _activeRequest.value ?: error("No active request")
        if (request.status != RequestStatus.BIDDING) error("Bidding has ended")
        firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .update("autoAcceptLowestBid", enabled)
            .await()
        _activeRequest.value = request.copy(autoAcceptLowestBid = enabled)
        if (enabled && _biddingSecondsLeft.value <= 0) {
            maybeAutoAcceptLowestBid()
        }
    }

    private suspend fun resolveShop(bid: MechanicBid): MechanicShop {
        BusinessRepository.instance.getShopById(bid.shopId)?.let { return it }
        return MechanicShop(
            id = bid.shopId,
            name = bid.shopName,
            rating = bid.shopRating,
            reviewCount = 0,
            isOnline = true,
            services = emptyList(),
            distanceKm = bid.distanceKm,
        )
    }

    suspend fun acceptBid(bid: MechanicBid) {
        val requestId = _activeRequestId.value ?: return
        val request = _activeRequest.value ?: return
        stopBiddingJobs()

        firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .update(
                mapOf(
                    "status" to RequestStatus.ACCEPTED.name,
                    "acceptedBid" to FirestoreMappers.acceptedBidToMap(bid),
                    "acceptedShopId" to bid.shopId,
                    "acceptedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

        val shop = resolveShop(bid)

        _acceptedJob.value = ActiveJob(
            request = request.copy(status = RequestStatus.ACCEPTED),
            acceptedBid = bid,
            mechanicShop = shop,
        )
        _activeRequest.value = request.copy(status = RequestStatus.ACCEPTED)
        _biddingSecondsLeft.value = 0
        refreshRequestHistory()
    }

    suspend fun cancelRequest() {
        val requestId = _activeRequestId.value
        stopBiddingJobs()
        if (requestId != null) {
            firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
                .document(requestId)
                .update("status", RequestStatus.CANCELLED.name)
                .await()
        }
        clearActiveSession()
        refreshRequestHistory()
    }

    suspend fun completeActiveJob() {
        val requestId = _activeRequestId.value ?: return
        firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .update("status", RequestStatus.COMPLETED.name)
            .await()
        clearActiveSession()
        refreshRequestHistory()
    }

    suspend fun refreshRequestHistory() {
        val userId = auth.currentUser?.uid ?: return
        _isLoadingHistory.value = true
        try {
            val snapshot = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            _requestHistory.value = snapshot.documents.mapNotNull { FirestoreMappers.requestFromDocument(it) }
        } finally {
            _isLoadingHistory.value = false
        }
    }

    suspend fun loadActiveSessionIfAny() {
        val userId = auth.currentUser?.uid ?: return
        val snapshot = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(15)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull { doc ->
            val status = doc.getString("status")
            status == RequestStatus.BIDDING.name ||
                status == RequestStatus.ACCEPTED.name ||
                status == RequestStatus.IN_PROGRESS.name
        } ?: return
        val request = FirestoreMappers.requestFromDocument(doc) ?: return

        _activeRequestId.value = doc.id
        _activeRequest.value = request
        attachRequestListener(doc.id)
        attachBidsListener(doc.id)

        val endsAt = request.biddingEndsAtMillis
        if (request.status == RequestStatus.BIDDING && endsAt > System.currentTimeMillis()) {
            _biddingSecondsLeft.value = ((endsAt - System.currentTimeMillis()) / 1000).toInt()
            currentBiddingEndsAtMillis = endsAt
            startBiddingTimer(endsAt)
        } else if (request.status == RequestStatus.BIDDING) {
            _biddingSecondsLeft.value = 0
            scope.launch { maybeAutoAcceptLowestBid() }
        }

        if (request.status == RequestStatus.ACCEPTED) {
            val acceptedMap = doc.get("acceptedBid") as? Map<String, Any>
            val bid = FirestoreMappers.acceptedBidFromMap(acceptedMap) ?: return
            scope.launch {
                val shop = resolveShop(bid)
                _acceptedJob.value = ActiveJob(request, bid, shop)
            }
        }
    }

    fun clearActiveSession() {
        stopBiddingJobs()
        clearListeners()
        _activeRequestId.value = null
        _activeRequest.value = null
        _bids.value = emptyList()
        _acceptedJob.value = null
        _biddingSecondsLeft.value = 0
    }

    private fun stopBiddingJobs() {
        biddingTimerJob?.cancel()
        biddingTimerJob = null
    }

    private fun clearListeners() {
        bidsListener?.remove()
        requestListener?.remove()
        bidsListener = null
        requestListener = null
    }
}

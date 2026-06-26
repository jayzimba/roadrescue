package com.jayjaycode.miniproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jayjaycode.miniproject.data.firebase.FirestoreConstants
import com.jayjaycode.miniproject.data.firebase.FirestoreMappers
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
import kotlin.random.Random

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
    private var bidSimulationJob: Job? = null

    private val _activeRequestId = MutableStateFlow<String?>(null)
    val activeRequestId: Flow<String?> = _activeRequestId.asStateFlow()

    private val _activeRequest = MutableStateFlow<BreakdownRequest?>(null)
    val activeRequest: Flow<BreakdownRequest?> = _activeRequest.asStateFlow()

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

    val onlineShops: List<MechanicShop> = MockRepository.onlineShops

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

    suspend fun submitBreakdownRequest(
        type: RequestType,
        vehicle: VehicleInfo,
        problemDescription: String,
        damageDescription: String,
        locationLabel: String,
        latitude: Double,
        longitude: Double,
        photoUris: List<String> = emptyList(),
    ): BreakdownRequest {
        val user = auth.currentUser ?: error("You must be signed in")
        val now = System.currentTimeMillis()
        val biddingEndsAt = now + FirestoreConstants.BIDDING_DURATION_SECONDS * 1000L

        val request = BreakdownRequest(
            id = "",
            type = type,
            vehicle = vehicle,
            problemDescription = problemDescription,
            damageDescription = damageDescription,
            locationLabel = locationLabel,
            latitude = latitude,
            longitude = longitude,
            photoUris = photoUris,
            status = RequestStatus.BIDDING,
            createdAtMillis = now,
            userId = user.uid,
            biddingEndsAtMillis = biddingEndsAt,
        )

        val docRef = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS).document()
        val requestWithId = request.copy(id = docRef.id)
        docRef.set(
            FirestoreMappers.requestToMap(requestWithId, user.uid, user.email.orEmpty(), biddingEndsAt),
        ).await()

        clearListeners()
        _activeRequestId.value = docRef.id
        _activeRequest.value = requestWithId
        _bids.value = emptyList()
        _acceptedJob.value = null
        _biddingSecondsLeft.value = FirestoreConstants.BIDDING_DURATION_SECONDS

        attachRequestListener(docRef.id)
        attachBidsListener(docRef.id)
        startBiddingTimer(biddingEndsAt)
        startBidSimulation(docRef.id, type)

        refreshRequestHistory()
        return requestWithId
    }

    private fun attachRequestListener(requestId: String) {
        requestListener?.remove()
        requestListener = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val request = FirestoreMappers.requestFromDocument(snapshot) ?: return@addSnapshotListener
                _activeRequest.value = request

                if (request.status == RequestStatus.ACCEPTED || request.status == RequestStatus.IN_PROGRESS) {
                    val acceptedMap = snapshot.get("acceptedBid") as? Map<String, Any>
                    val bid = FirestoreMappers.acceptedBidFromMap(acceptedMap) ?: return@addSnapshotListener
                    val shop = onlineShops.find { it.id == bid.shopId }
                        ?: MechanicShop(
                            id = bid.shopId,
                            name = bid.shopName,
                            rating = bid.shopRating,
                            reviewCount = 0,
                            isOnline = true,
                            services = emptyList(),
                            distanceKm = bid.distanceKm,
                        )
                    _acceptedJob.value = ActiveJob(request = request, acceptedBid = bid, mechanicShop = shop)
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
        biddingTimerJob?.cancel()
        biddingTimerJob = scope.launch {
            while (true) {
                val secondsLeft = ((biddingEndsAtMillis - System.currentTimeMillis()) / 1000).toInt()
                _biddingSecondsLeft.value = secondsLeft.coerceAtLeast(0)
                if (secondsLeft <= 0) break
                delay(1000)
            }
            _biddingSecondsLeft.value = 0
            // UI shows timer at zero; request stays BIDDING until user accepts or cancels.
        }
    }

    private fun startBidSimulation(requestId: String, type: RequestType) {
        bidSimulationJob?.cancel()
        bidSimulationJob = scope.launch {
            val shops = onlineShops.filter { it.isOnline }
            shops.forEachIndexed { index, shop ->
                if (_acceptedJob.value != null) return@launch
                delay(3000L * (index + 1))
                if (_activeRequestId.value != requestId) return@launch

                val bid = MechanicBid(
                    id = "",
                    shopId = shop.id,
                    shopName = shop.name,
                    shopRating = shop.rating,
                    distanceKm = shop.distanceKm + Random.nextDouble(-0.5, 0.5),
                    etaMinutes = (shop.distanceKm * 4).toInt() + Random.nextInt(5, 15),
                    price = when (type) {
                        RequestType.TOWING -> Random.nextDouble(1200.0, 3500.0)
                        else -> Random.nextDouble(800.0, 2800.0)
                    },
                    message = listOf(
                        "We can be there fast!",
                        "Experienced with this issue",
                        "Best price in your area",
                        "Available now — ready to dispatch",
                    ).random(),
                )

                firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
                    .document(requestId)
                    .collection(FirestoreConstants.BIDS)
                    .add(FirestoreMappers.bidToMap(bid))
                    .await()
            }
        }
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
                    "acceptedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()

        val shop = onlineShops.find { it.id == bid.shopId }
            ?: MechanicShop(
                id = bid.shopId,
                name = bid.shopName,
                rating = bid.shopRating,
                reviewCount = 0,
                isOnline = true,
                services = emptyList(),
                distanceKm = bid.distanceKm,
            )

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
            startBiddingTimer(endsAt)
        }

        if (request.status == RequestStatus.ACCEPTED) {
            val acceptedMap = doc.get("acceptedBid") as? Map<String, Any>
            val bid = FirestoreMappers.acceptedBidFromMap(acceptedMap) ?: return
            val shop = onlineShops.find { it.id == bid.shopId }
                ?: MechanicShop(bid.shopId, bid.shopName, bid.shopRating, 0, true, emptyList(), bid.distanceKm)
            _acceptedJob.value = ActiveJob(request, bid, shop)
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
        bidSimulationJob?.cancel()
        biddingTimerJob = null
        bidSimulationJob = null
    }

    private fun clearListeners() {
        bidsListener?.remove()
        requestListener?.remove()
        bidsListener = null
        requestListener = null
    }
}

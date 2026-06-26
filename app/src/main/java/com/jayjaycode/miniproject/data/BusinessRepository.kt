package com.jayjaycode.miniproject.data

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jayjaycode.miniproject.data.firebase.FirestoreConstants
import com.jayjaycode.miniproject.data.firebase.FirestoreMappers
import com.jayjaycode.miniproject.util.DocumentPickerUtils
import com.jayjaycode.miniproject.util.FirebaseStorageHelper
import android.util.Base64
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BusinessRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    companion object {
        val instance = BusinessRepository()
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: Flow<UserProfile?> = _userProfile.asStateFlow()

    private val _myBusiness = MutableStateFlow<BusinessProfile?>(null)
    val myBusiness: Flow<BusinessProfile?> = _myBusiness.asStateFlow()

    private var profileListener: ListenerRegistration? = null
    private var businessListener: ListenerRegistration? = null

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: error("You must be signed in")

    fun startObservingProfile() {
        val uid = auth.currentUser?.uid ?: return
        profileListener?.remove()
        profileListener = firestore.collection(FirestoreConstants.USERS)
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                _userProfile.value = snapshot?.let { FirestoreMappers.userProfileFromDocument(it) }
                val shopId = snapshot?.getString("shopId").orEmpty()
                if (shopId.isNotBlank()) {
                    observeBusiness(shopId)
                } else {
                    businessListener?.remove()
                    _myBusiness.value = null
                }
            }
    }

    private fun observeBusiness(shopId: String) {
        businessListener?.remove()
        businessListener = firestore.collection(FirestoreConstants.SHOPS)
            .document(shopId)
            .addSnapshotListener { snapshot, _ ->
                _myBusiness.value = snapshot?.let { FirestoreMappers.businessFromDocument(it) }
            }
    }

    fun stopObserving() {
        profileListener?.remove()
        businessListener?.remove()
        profileListener = null
        businessListener = null
        _userProfile.value = null
        _myBusiness.value = null
    }

    suspend fun updatePhone(phone: String) {
        val uid = requireUserId()
        firestore.collection(FirestoreConstants.USERS)
            .document(uid)
            .update("phone", phone.trim())
            .await()
    }

    suspend fun registerBusiness(
        businessType: BusinessType,
        businessName: String,
        description: String,
        phone: String,
        address: String,
        services: List<String>,
        certificateUri: Uri,
        certificateFileName: String,
        context: Context,
    ): BusinessProfile {
        val uid = requireUserId()
        val user = auth.currentUser ?: error("You must be signed in")
        val existingShopId = _userProfile.value?.shopId.orEmpty()
        if (existingShopId.isNotBlank()) error("You already have a registered business")

        DocumentPickerUtils.requirePdfDocument(context, certificateUri)

        val now = System.currentTimeMillis()
        val shopRef = firestore.collection(FirestoreConstants.SHOPS).document()
        val resolvedFileName = certificateFileName.ifBlank { "registration_certificate.pdf" }

        val business = BusinessProfile(
            id = shopRef.id,
            ownerId = uid,
            businessType = businessType,
            businessName = businessName.trim(),
            description = description.trim(),
            phone = phone.trim(),
            address = address.trim(),
            services = services,
            registrationCertificateFileName = resolvedFileName,
            isOnline = false,
            createdAtMillis = now,
        )

        // Shop doc must exist before private certificate subcollection writes (Firestore rules).
        shopRef.set(FirestoreMappers.businessToMap(business)).await()

        val certificate = saveRegistrationCertificate(
            ownerId = uid,
            shopId = shopRef.id,
            certificateUri = certificateUri,
            certificateFileName = resolvedFileName,
            context = context,
        )

        val completedBusiness = business.copy(registrationCertificateUrl = certificate.referenceUrl)
        shopRef.update(
            mapOf(
                "registrationCertificateUrl" to certificate.referenceUrl,
                "registrationCertificateFileName" to resolvedFileName,
            ) + certificate.shopFields,
        ).await()
        firestore.collection(FirestoreConstants.USERS)
            .document(uid)
            .set(
                mapOf(
                    "displayName" to user.displayName.orEmpty(),
                    "email" to user.email.orEmpty(),
                    "phone" to phone.trim(),
                    "shopId" to shopRef.id,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            ).await()
        _myBusiness.value = completedBusiness
        return completedBusiness
    }

    private data class CertificateSaveResult(
        val referenceUrl: String,
        val shopFields: Map<String, Any?> = emptyMap(),
    )

    private suspend fun saveRegistrationCertificate(
        ownerId: String,
        shopId: String,
        certificateUri: Uri,
        certificateFileName: String,
        context: Context,
    ): CertificateSaveResult {
        return try {
            val url = FirebaseStorageHelper.uploadPdf(
                pathSegments = listOf(
                    FirestoreConstants.BUSINESS_CERTIFICATES_STORAGE,
                    ownerId,
                    shopId,
                    "registration_certificate.pdf",
                ),
                sourceUri = certificateUri,
                context = context,
                fileName = certificateFileName,
            )
            CertificateSaveResult(
                referenceUrl = url,
                shopFields = mapOf("registrationCertificateStorage" to "firebase_storage"),
            )
        } catch (e: Exception) {
            if (!FirebaseStorageHelper.isStorageNotConfigured(e)) throw e
            embedCertificateInFirestore(
                shopId = shopId,
                ownerId = ownerId,
                certificateUri = certificateUri,
                certificateFileName = certificateFileName,
                context = context,
            )
        }
    }

    private suspend fun embedCertificateInFirestore(
        shopId: String,
        ownerId: String,
        certificateUri: Uri,
        certificateFileName: String,
        context: Context,
    ): CertificateSaveResult {
        val bytes = FirebaseStorageHelper.readPdfBytes(context, certificateUri)
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        firestore.collection(FirestoreConstants.SHOPS)
            .document(shopId)
            .collection(FirestoreConstants.SHOP_PRIVATE)
            .document(FirestoreConstants.SHOP_CERTIFICATE_DOC)
            .set(
                mapOf(
                    "ownerId" to ownerId,
                    "fileName" to certificateFileName.ifBlank { "registration_certificate.pdf" },
                    "contentType" to "application/pdf",
                    "contentBase64" to encoded,
                    "uploadedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
        return CertificateSaveResult(
            referenceUrl = "firestore://shops/$shopId/${FirestoreConstants.SHOP_PRIVATE}/${FirestoreConstants.SHOP_CERTIFICATE_DOC}",
            shopFields = mapOf(
                "registrationCertificateStorage" to "firestore",
                "registrationCertificatePendingStorage" to true,
            ),
        )
    }

    suspend fun setOnlineStatus(isOnline: Boolean) {
        val shop = _myBusiness.value ?: error("No business registered")
        firestore.collection(FirestoreConstants.SHOPS)
            .document(shop.id)
            .update("isOnline", isOnline)
            .await()
    }

    suspend fun addPartListing(
        name: String,
        category: String,
        price: Double,
        condition: String,
        compatibleVehicles: List<VehicleCompatibility>,
        photoUris: List<Uri>,
        paymentMethods: List<PaymentMethod>,
        context: Context,
    ): SparePart {
        val shop = _myBusiness.value ?: error("Register a business first")
        val uid = requireUserId()
        if (photoUris.isEmpty()) error("Add at least one photo of the spare part")
        if (paymentMethods.isEmpty()) error("Select at least one accepted payment method")
        if (compatibleVehicles.isEmpty()) error("Add at least one compatible make and model")

        val ref = firestore.collection(FirestoreConstants.PART_LISTINGS).document()
        val imageUrls = photoUris.mapIndexed { index, uri ->
            FirebaseStorageHelper.uploadImage(
                pathSegments = listOf(
                    FirestoreConstants.PART_LISTING_IMAGES,
                    uid,
                    shop.id,
                    ref.id,
                    "image_$index.jpg",
                ),
                sourceUri = uri,
                context = context,
            )
        }

        val part = SparePart(
            id = ref.id,
            name = name.trim(),
            category = category.trim(),
            price = price,
            seller = shop.businessName,
            condition = condition.trim(),
            compatibleVehicles = compatibleVehicles,
            inStock = true,
            shopId = shop.id,
            ownerId = uid,
            imageUrls = imageUrls,
            paymentMethods = paymentMethods,
        )
        firestore.collection(FirestoreConstants.PART_LISTINGS)
            .document(ref.id)
            .set(FirestoreMappers.partListingToMap(part, shop.businessName))
            .await()
        return part
    }

    suspend fun addServiceListing(
        name: String,
        description: String,
        durationMinutes: Int,
        price: Double,
        includes: List<String>,
    ): ServicePackage {
        val shop = _myBusiness.value ?: error("Register a business first")
        val uid = requireUserId()
        val service = ServicePackage(
            id = "",
            name = name.trim(),
            description = description.trim(),
            durationMinutes = durationMinutes,
            priceFrom = price,
            includes = includes,
            shopId = shop.id,
            shopName = shop.businessName,
            ownerId = uid,
        )
        val ref = firestore.collection(FirestoreConstants.SERVICE_LISTINGS).document()
        firestore.collection(FirestoreConstants.SERVICE_LISTINGS)
            .document(ref.id)
            .set(FirestoreMappers.serviceListingToMap(service.copy(id = ref.id)))
            .await()
        return service.copy(id = ref.id)
    }

    suspend fun togglePartStock(partId: String, inStock: Boolean) {
        firestore.collection(FirestoreConstants.PART_LISTINGS)
            .document(partId)
            .update("inStock", inStock)
            .await()
    }

    fun observeMarketplaceParts(): Flow<List<SparePart>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.PART_LISTINGS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val parts = snapshot?.documents
                    ?.mapNotNull { FirestoreMappers.partListingFromDocument(it) }
                    ?.filter { it.inStock }
                    .orEmpty()
                trySend(parts)
            }
        awaitClose { listener.remove() }
    }

    fun observePartListings(): Flow<List<SparePart>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.PART_LISTINGS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val parts = snapshot?.documents?.mapNotNull { FirestoreMappers.partListingFromDocument(it) }
                    .orEmpty()
                trySend(parts)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyPartListings(shopId: String): Flow<List<SparePart>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.PART_LISTINGS)
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, _ ->
                val parts = snapshot?.documents?.mapNotNull { FirestoreMappers.partListingFromDocument(it) }
                    .orEmpty()
                trySend(parts)
            }
        awaitClose { listener.remove() }
    }

    fun observeServiceListings(): Flow<List<ServicePackage>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.SERVICE_LISTINGS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val services = snapshot?.documents?.mapNotNull { FirestoreMappers.serviceListingFromDocument(it) }
                    .orEmpty()
                trySend(services)
            }
        awaitClose { listener.remove() }
    }

    fun observeMyServiceListings(shopId: String): Flow<List<ServicePackage>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.SERVICE_LISTINGS)
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, _ ->
                val services = snapshot?.documents?.mapNotNull { FirestoreMappers.serviceListingFromDocument(it) }
                    .orEmpty()
                trySend(services)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPartOrder(
        items: List<SparePart>,
        paymentMethod: PaymentMethod,
        deliveryPhone: String,
        deliveryAddress: String,
    ): PartOrder {
        val user = auth.currentUser ?: error("You must be signed in")
        if (items.isEmpty()) error("Cart is empty")
        val shopId = items.first().shopId
        val shopOwnerId = items.first().ownerId
        val shopName = items.first().seller
        if (items.any { it.shopId != shopId }) {
            error("Checkout one shop at a time")
        }
        val order = PartOrder(
            id = "",
            buyerId = user.uid,
            buyerEmail = user.email.orEmpty(),
            shopId = shopId,
            shopName = shopName,
            shopOwnerId = shopOwnerId,
            items = items,
            totalPrice = items.sumOf { it.price },
            paymentMethod = paymentMethod,
            deliveryPhone = deliveryPhone.trim(),
            deliveryAddress = deliveryAddress.trim(),
        )
        val ref = firestore.collection(FirestoreConstants.ORDERS).document()
        firestore.collection(FirestoreConstants.ORDERS)
            .document(ref.id)
            .set(FirestoreMappers.partOrderToMap(order.copy(id = ref.id)))
            .await()
        return order.copy(id = ref.id)
    }

    suspend fun createServiceBooking(
        service: ServicePackage,
        vehicleNote: String,
        preferredDate: String,
    ): ServiceBookingOrder {
        val user = auth.currentUser ?: error("You must be signed in")
        val booking = ServiceBookingOrder(
            id = "",
            buyerId = user.uid,
            buyerEmail = user.email.orEmpty(),
            shopId = service.shopId,
            shopName = service.shopName,
            shopOwnerId = service.ownerId,
            serviceId = service.id,
            serviceName = service.name,
            vehicleNote = vehicleNote.trim(),
            preferredDate = preferredDate.trim(),
            price = service.priceFrom,
        )
        val ref = firestore.collection(FirestoreConstants.SERVICE_BOOKINGS).document()
        firestore.collection(FirestoreConstants.SERVICE_BOOKINGS)
            .document(ref.id)
            .set(FirestoreMappers.serviceBookingToMap(booking.copy(id = ref.id)))
            .await()
        return booking.copy(id = ref.id)
    }

    fun observeIncomingOrders(shopOwnerId: String): Flow<List<PartOrder>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.ORDERS)
            .whereEqualTo("shopOwnerId", shopOwnerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { FirestoreMappers.partOrderFromDocument(it) }
                    .orEmpty()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun observeIncomingBookings(shopOwnerId: String): Flow<List<ServiceBookingOrder>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.SERVICE_BOOKINGS)
            .whereEqualTo("shopOwnerId", shopOwnerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val bookings = snapshot?.documents?.mapNotNull { FirestoreMappers.serviceBookingFromDocument(it) }
                    .orEmpty()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        firestore.collection(FirestoreConstants.ORDERS)
            .document(orderId)
            .update("status", status.name)
            .await()
    }

    suspend fun updateBookingStatus(bookingId: String, status: OrderStatus) {
        firestore.collection(FirestoreConstants.SERVICE_BOOKINGS)
            .document(bookingId)
            .update("status", status.name)
            .await()
    }

    fun observeOpenBreakdownRequests(): Flow<List<BreakdownRequest>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .whereEqualTo("status", RequestStatus.BIDDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.documents?.mapNotNull { FirestoreMappers.requestFromDocument(it) }
                    .orEmpty()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    fun observeProviderJobs(shopId: String): Flow<List<BreakdownRequest>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .whereEqualTo("acceptedShopId", shopId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.documents?.mapNotNull { FirestoreMappers.requestFromDocument(it) }
                    .orEmpty()
                    .filter {
                        it.status == RequestStatus.ACCEPTED ||
                            it.status == RequestStatus.IN_PROGRESS
                    }
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    suspend fun placeProviderBid(
        requestId: String,
        price: Double,
        etaMinutes: Int,
        message: String,
    ) {
        val shop = _myBusiness.value ?: error("Register a business first")
        if (!shop.isOnline) error("Go online to place bids")
        val bid = MechanicBid(
            id = "",
            shopId = shop.id,
            shopName = shop.businessName,
            shopRating = shop.rating,
            distanceKm = 0.0,
            etaMinutes = etaMinutes,
            price = price,
            message = message.trim(),
        )
        firestore.collection(FirestoreConstants.BREAKDOWN_REQUESTS)
            .document(requestId)
            .collection(FirestoreConstants.BIDS)
            .add(FirestoreMappers.bidToMap(bid))
            .await()
    }

    suspend fun getOnlineShops(): List<MechanicShop> {
        val snapshot = firestore.collection(FirestoreConstants.SHOPS)
            .whereEqualTo("isOnline", true)
            .get()
            .await()
        return snapshot.documents.mapNotNull { FirestoreMappers.shopFromDocument(it) }
    }

    suspend fun getShopById(shopId: String): MechanicShop? {
        val doc = firestore.collection(FirestoreConstants.SHOPS).document(shopId).get().await()
        return FirestoreMappers.shopFromDocument(doc)
    }

    fun observeRegisteredShops(): Flow<List<MechanicShop>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.SHOPS)
            .addSnapshotListener { snapshot, _ ->
                val shops = snapshot?.documents
                    ?.mapNotNull { FirestoreMappers.shopFromDocument(it) }
                    ?.sortedWith(
                        compareByDescending<MechanicShop> { it.isOnline }
                            .thenBy { it.name },
                    )
                    .orEmpty()
                trySend(shops)
            }
        awaitClose { listener.remove() }
    }

    fun clearSession() {
        stopObserving()
    }
}

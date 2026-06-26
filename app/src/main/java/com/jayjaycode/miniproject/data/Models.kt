package com.jayjaycode.miniproject.data

enum class RequestType { TOWING, MECHANIC }

enum class RequestStatus { DRAFT, BIDDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED }

enum class BusinessType(val label: String, val description: String) {
    AUTO_COMPANY("Auto company", "Fleet towing & roadside recovery"),
    MECHANIC("Mobile mechanic", "On-site repairs and diagnostics"),
    AUTO_SHOP("Auto shop", "Garage services and spare parts"),
}

enum class OrderStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED }

enum class PaymentMethod(val label: String) {
    AIRTEL("Airtel Money"),
    CARD("Card"),
    CASH_ON_DELIVERY("Cash on delivery"),
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val phone: String = "",
    val shopId: String = "",
    val createdAtMillis: Long = 0L,
)

data class BusinessProfile(
    val id: String,
    val ownerId: String,
    val businessType: BusinessType,
    val businessName: String,
    val description: String,
    val phone: String,
    val address: String,
    val services: List<String>,
    val registrationCertificateUrl: String = "",
    val registrationCertificateFileName: String = "",
    val isOnline: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val createdAtMillis: Long = 0L,
)

data class VehicleInfo(
    val make: String,
    val model: String,
    val year: String,
    val plateNumber: String,
    val color: String = "",
)

data class VehicleCompatibility(
    val make: String,
    val model: String,
) {
    val isUniversal: Boolean
        get() = make == ALL_MAKES && model == ALL_MODELS

    fun displayLabel(): String = when {
        isUniversal -> "All vehicles"
        make != ALL_MAKES && model == ALL_MODELS -> "$make (all models)"
        else -> "$make $model"
    }

    companion object {
        const val ALL_MAKES = "All makes"
        const val ALL_MODELS = "All models"
    }
}

data class BreakdownRequest(
    val id: String,
    val type: RequestType,
    val vehicle: VehicleInfo,
    val problemDescription: String,
    val damageDescription: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val photoUris: List<String> = emptyList(),
    val status: RequestStatus = RequestStatus.DRAFT,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val userId: String = "",
    val biddingEndsAtMillis: Long = 0L,
    val autoAcceptLowestBid: Boolean = false,
    val acceptedShopId: String = "",
)

enum class ProviderBidOutcome {
    PENDING,
    WON,
    LOST,
    CLOSED,
}

data class ProviderBidEntry(
    val requestId: String,
    val request: BreakdownRequest?,
    val bid: MechanicBid,
) {
    val outcome: ProviderBidOutcome
        get() {
            val req = request ?: return ProviderBidOutcome.PENDING
            return when (req.status) {
                RequestStatus.BIDDING -> ProviderBidOutcome.PENDING
                RequestStatus.ACCEPTED, RequestStatus.IN_PROGRESS ->
                    if (req.acceptedShopId == bid.shopId) ProviderBidOutcome.WON else ProviderBidOutcome.LOST
                else -> ProviderBidOutcome.CLOSED
            }
        }
}

data class MechanicBid(
    val id: String,
    val shopId: String,
    val shopName: String,
    val shopRating: Float,
    val distanceKm: Double,
    val etaMinutes: Int,
    val price: Double,
    val message: String,
    val isOnline: Boolean = true,
)

data class MechanicShop(
    val id: String,
    val name: String,
    val rating: Float,
    val reviewCount: Int,
    val isOnline: Boolean,
    val services: List<String>,
    val distanceKm: Double,
)

data class SparePart(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val seller: String,
    val condition: String,
    val compatibleVehicles: List<VehicleCompatibility>,
    val inStock: Boolean,
    val quantity: Int? = null,
    val shopId: String = "",
    val ownerId: String = "",
    val imageUrls: List<String> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
)

data class CartLineItem(
    val part: SparePart,
    val quantity: Int,
) {
    val lineTotal: Double get() = part.price * quantity
}

data class PartOrderLineItem(
    val partId: String,
    val name: String,
    val category: String,
    val unitPrice: Double,
    val quantity: Int,
) {
    val lineTotal: Double get() = unitPrice * quantity
}

data class PartOrder(
    val id: String,
    val buyerId: String,
    val buyerEmail: String,
    val shopId: String,
    val shopName: String,
    val shopOwnerId: String,
    val items: List<PartOrderLineItem>,
    val totalPrice: Double,
    val paymentMethod: PaymentMethod? = null,
    val deliveryPhone: String = "",
    val deliveryAddress: String = "",
    val deliveryLatitude: Double? = null,
    val deliveryLongitude: Double? = null,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class ServicePackage(
    val id: String,
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val priceFrom: Double,
    val includes: List<String>,
    val shopId: String = "",
    val shopName: String = "",
    val ownerId: String = "",
)

data class ServiceBookingOrder(
    val id: String,
    val buyerId: String,
    val buyerEmail: String,
    val shopId: String,
    val shopName: String,
    val shopOwnerId: String,
    val serviceId: String,
    val serviceName: String,
    val vehicleNote: String,
    val preferredDate: String,
    val price: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class ActiveJob(
    val request: BreakdownRequest,
    val acceptedBid: MechanicBid,
    val mechanicShop: MechanicShop,
)

package com.jayjaycode.miniproject.data

enum class RequestType { TOWING, MECHANIC }

enum class RequestStatus { DRAFT, BIDDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED }

data class VehicleInfo(
    val make: String,
    val model: String,
    val year: String,
    val plateNumber: String,
    val color: String = "",
)

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
)

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
    val compatibleMakes: List<String>,
    val inStock: Boolean,
)

data class ServicePackage(
    val id: String,
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val priceFrom: Double,
    val includes: List<String>,
)

data class ActiveJob(
    val request: BreakdownRequest,
    val acceptedBid: MechanicBid,
    val mechanicShop: MechanicShop,
)

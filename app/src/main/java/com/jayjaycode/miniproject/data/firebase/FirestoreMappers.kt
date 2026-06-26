package com.jayjaycode.miniproject.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.data.MechanicShop
import com.jayjaycode.miniproject.data.RequestStatus
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.VehicleInfo

object FirestoreMappers {

    fun requestToMap(
        request: BreakdownRequest,
        userId: String,
        userEmail: String,
        biddingEndsAtMillis: Long,
    ): Map<String, Any?> = mapOf(
        "userId" to userId,
        "userEmail" to userEmail,
        "type" to request.type.name,
        "vehicleMake" to request.vehicle.make,
        "vehicleModel" to request.vehicle.model,
        "vehicleYear" to request.vehicle.year,
        "vehiclePlate" to request.vehicle.plateNumber,
        "vehicleColor" to request.vehicle.color,
        "problemDescription" to request.problemDescription,
        "damageDescription" to request.damageDescription,
        "locationLabel" to request.locationLabel,
        "latitude" to request.latitude,
        "longitude" to request.longitude,
        "photoUris" to request.photoUris,
        "status" to request.status.name,
        "createdAt" to Timestamp(Date(request.createdAtMillis)),
        "biddingEndsAt" to Timestamp(Date(biddingEndsAtMillis)),
    )

    fun requestFromDocument(doc: DocumentSnapshot): BreakdownRequest? {
        if (!doc.exists()) return null
        val type = runCatching { RequestType.valueOf(doc.getString("type") ?: return null) }.getOrNull() ?: return null
        val status = runCatching {
            RequestStatus.valueOf(doc.getString("status") ?: RequestStatus.DRAFT.name)
        }.getOrElse { RequestStatus.DRAFT }

        return BreakdownRequest(
            id = doc.id,
            type = type,
            vehicle = VehicleInfo(
                make = doc.getString("vehicleMake").orEmpty(),
                model = doc.getString("vehicleModel").orEmpty(),
                year = doc.getString("vehicleYear").orEmpty(),
                plateNumber = doc.getString("vehiclePlate").orEmpty(),
                color = doc.getString("vehicleColor").orEmpty(),
            ),
            problemDescription = doc.getString("problemDescription").orEmpty(),
            damageDescription = doc.getString("damageDescription").orEmpty(),
            locationLabel = doc.getString("locationLabel").orEmpty(),
            latitude = doc.getDouble("latitude") ?: 0.0,
            longitude = doc.getDouble("longitude") ?: 0.0,
            photoUris = (doc.get("photoUris") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            status = status,
            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
            userId = doc.getString("userId").orEmpty(),
            biddingEndsAtMillis = doc.getTimestamp("biddingEndsAt")?.toDate()?.time ?: 0L,
        )
    }

    fun bidToMap(bid: MechanicBid): Map<String, Any> = mapOf(
        "shopId" to bid.shopId,
        "shopName" to bid.shopName,
        "shopRating" to bid.shopRating,
        "distanceKm" to bid.distanceKm,
        "etaMinutes" to bid.etaMinutes,
        "price" to bid.price,
        "message" to bid.message,
        "createdAt" to Timestamp.now(),
    )

    fun bidFromDocument(doc: DocumentSnapshot): MechanicBid? {
        if (!doc.exists()) return null
        return MechanicBid(
            id = doc.id,
            shopId = doc.getString("shopId").orEmpty(),
            shopName = doc.getString("shopName").orEmpty(),
            shopRating = doc.getDouble("shopRating")?.toFloat() ?: 0f,
            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
            etaMinutes = doc.getLong("etaMinutes")?.toInt() ?: 0,
            price = doc.getDouble("price") ?: 0.0,
            message = doc.getString("message").orEmpty(),
        )
    }

    fun acceptedBidToMap(bid: MechanicBid): Map<String, Any> = mapOf(
        "bidId" to bid.id,
        "shopId" to bid.shopId,
        "shopName" to bid.shopName,
        "shopRating" to bid.shopRating,
        "distanceKm" to bid.distanceKm,
        "etaMinutes" to bid.etaMinutes,
        "price" to bid.price,
        "message" to bid.message,
    )

    fun acceptedBidFromMap(map: Map<String, Any>?): MechanicBid? {
        if (map == null) return null
        return MechanicBid(
            id = map["bidId"] as? String ?: "",
            shopId = map["shopId"] as? String ?: "",
            shopName = map["shopName"] as? String ?: "",
            shopRating = (map["shopRating"] as? Number)?.toFloat() ?: 0f,
            distanceKm = (map["distanceKm"] as? Number)?.toDouble() ?: 0.0,
            etaMinutes = (map["etaMinutes"] as? Number)?.toInt() ?: 0,
            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
            message = map["message"] as? String ?: "",
        )
    }

    fun shopFromDocument(doc: DocumentSnapshot): MechanicShop? {
        if (!doc.exists()) return null
        return MechanicShop(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
            isOnline = doc.getBoolean("isOnline") ?: false,
            services = (doc.get("services") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
        )
    }
}

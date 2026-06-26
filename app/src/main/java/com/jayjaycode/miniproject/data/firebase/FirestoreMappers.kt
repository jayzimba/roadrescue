package com.jayjaycode.miniproject.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import java.util.Date
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.BusinessProfile
import com.jayjaycode.miniproject.data.BusinessType
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.data.MechanicShop
import com.jayjaycode.miniproject.data.OrderStatus
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.PartOrderLineItem
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.RequestStatus
import com.jayjaycode.miniproject.data.CompletionParty
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.ServiceBookingOrder
import com.jayjaycode.miniproject.data.ServicePackage
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.data.UserProfile
import com.jayjaycode.miniproject.data.VehicleCompatibility
import com.jayjaycode.miniproject.data.VehicleInfo

object FirestoreMappers {

    private fun compatibleVehiclesFromDocument(doc: DocumentSnapshot): List<VehicleCompatibility> {
        val structured = (doc.get("compatibleVehicles") as? List<*>)?.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val make = map["make"] as? String ?: return@mapNotNull null
            val model = map["model"] as? String ?: return@mapNotNull null
            VehicleCompatibility(make, model)
        }.orEmpty()
        if (structured.isNotEmpty()) return structured

        return (doc.get("compatibleMakes") as? List<*>)?.filterIsInstance<String>()?.map { make ->
            VehicleCompatibility(make, VehicleCompatibility.ALL_MODELS)
        }.orEmpty()
    }

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
        "autoAcceptLowestBid" to request.autoAcceptLowestBid,
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
            autoAcceptLowestBid = doc.getBoolean("autoAcceptLowestBid") ?: false,
            acceptedShopId = doc.getString("acceptedShopId").orEmpty(),
            completionRequestedBy = doc.getString("completionRequestedBy")?.let { value ->
                runCatching { CompletionParty.valueOf(value) }.getOrNull()
            },
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
            name = doc.getString("businessName") ?: doc.getString("name").orEmpty(),
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
            isOnline = doc.getBoolean("isOnline") ?: false,
            services = (doc.get("services") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
        )
    }

    fun userProfileFromDocument(doc: DocumentSnapshot): UserProfile? {
        if (!doc.exists()) return null
        return UserProfile(
            uid = doc.id,
            displayName = doc.getString("displayName").orEmpty(),
            email = doc.getString("email").orEmpty(),
            phone = doc.getString("phone").orEmpty(),
            shopId = doc.getString("shopId").orEmpty(),
            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }

    fun businessFromDocument(doc: DocumentSnapshot): BusinessProfile? {
        if (!doc.exists()) return null
        val type = runCatching {
            BusinessType.valueOf(doc.getString("businessType") ?: return null)
        }.getOrNull() ?: return null
        return BusinessProfile(
            id = doc.id,
            ownerId = doc.getString("ownerId").orEmpty(),
            businessType = type,
            businessName = doc.getString("businessName").orEmpty(),
            description = doc.getString("description").orEmpty(),
            phone = doc.getString("phone").orEmpty(),
            address = doc.getString("address").orEmpty(),
            services = (doc.get("services") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            registrationCertificateUrl = doc.getString("registrationCertificateUrl").orEmpty(),
            registrationCertificateFileName = doc.getString("registrationCertificateFileName").orEmpty(),
            isOnline = doc.getBoolean("isOnline") ?: false,
            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }

    fun businessToMap(business: BusinessProfile): Map<String, Any?> = mapOf(
        "ownerId" to business.ownerId,
        "businessType" to business.businessType.name,
        "businessName" to business.businessName,
        "description" to business.description,
        "phone" to business.phone,
        "address" to business.address,
        "services" to business.services,
        "registrationCertificateUrl" to business.registrationCertificateUrl,
        "registrationCertificateFileName" to business.registrationCertificateFileName,
        "isOnline" to business.isOnline,
        "rating" to business.rating,
        "reviewCount" to business.reviewCount,
        "createdAt" to Timestamp(Date(business.createdAtMillis)),
    )

    fun partListingFromDocument(doc: DocumentSnapshot): SparePart? {
        if (!doc.exists()) return null
        return SparePart(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            category = doc.getString("category").orEmpty(),
            price = doc.getDouble("price") ?: 0.0,
            seller = doc.getString("shopName").orEmpty(),
            condition = doc.getString("condition").orEmpty(),
            compatibleVehicles = compatibleVehiclesFromDocument(doc),
            inStock = doc.getBoolean("inStock") ?: true,
            quantity = doc.getLong("quantity")?.toInt(),
            shopId = doc.getString("shopId").orEmpty(),
            ownerId = doc.getString("ownerId").orEmpty(),
            imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            paymentMethods = (doc.get("paymentMethods") as? List<*>)?.mapNotNull { raw ->
                runCatching { PaymentMethod.valueOf(raw as String) }.getOrNull()
            }.orEmpty(),
        )
    }

    fun partListingToMap(part: SparePart, shopName: String): Map<String, Any?> = mapOf(
        "name" to part.name,
        "category" to part.category,
        "price" to part.price,
        "shopId" to part.shopId,
        "shopName" to shopName,
        "ownerId" to part.ownerId,
        "condition" to part.condition,
        "compatibleVehicles" to part.compatibleVehicles.map { mapOf("make" to it.make, "model" to it.model) },
        "compatibleMakes" to part.compatibleVehicles.map { it.displayLabel() },
        "inStock" to part.inStock,
        "quantity" to part.quantity,
        "imageUrls" to part.imageUrls,
        "paymentMethods" to part.paymentMethods.map { it.name },
        "createdAt" to FieldValue.serverTimestamp(),
    )

    fun serviceListingFromDocument(doc: DocumentSnapshot): ServicePackage? {
        if (!doc.exists()) return null
        return ServicePackage(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            description = doc.getString("description").orEmpty(),
            durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 60,
            priceFrom = doc.getDouble("price") ?: 0.0,
            includes = (doc.get("includes") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            shopId = doc.getString("shopId").orEmpty(),
            shopName = doc.getString("shopName").orEmpty(),
            ownerId = doc.getString("ownerId").orEmpty(),
        )
    }

    fun serviceListingToMap(service: ServicePackage): Map<String, Any?> = mapOf(
        "name" to service.name,
        "description" to service.description,
        "durationMinutes" to service.durationMinutes,
        "price" to service.priceFrom,
        "includes" to service.includes,
        "shopId" to service.shopId,
        "shopName" to service.shopName,
        "ownerId" to service.ownerId,
        "createdAt" to FieldValue.serverTimestamp(),
    )

    fun partOrderToMap(order: PartOrder): Map<String, Any?> = mapOf(
        "buyerId" to order.buyerId,
        "buyerEmail" to order.buyerEmail,
        "shopId" to order.shopId,
        "shopName" to order.shopName,
        "shopOwnerId" to order.shopOwnerId,
        "items" to order.items.map { mapOf(
            "id" to it.partId,
            "name" to it.name,
            "price" to it.unitPrice,
            "category" to it.category,
            "quantity" to it.quantity,
        ) },
        "totalPrice" to order.totalPrice,
        "paymentMethod" to order.paymentMethod?.name,
        "deliveryPhone" to order.deliveryPhone,
        "deliveryAddress" to order.deliveryAddress,
        "deliveryLatitude" to order.deliveryLatitude,
        "deliveryLongitude" to order.deliveryLongitude,
        "status" to order.status.name,
        "createdAt" to Timestamp(Date(order.createdAtMillis)),
    )

    fun partOrderFromDocument(doc: DocumentSnapshot): PartOrder? {
        if (!doc.exists()) return null
        val status = runCatching {
            OrderStatus.valueOf(doc.getString("status") ?: OrderStatus.PENDING.name)
        }.getOrElse { OrderStatus.PENDING }
        val itemsRaw = doc.get("items") as? List<*> ?: emptyList<Any>()
        val items = itemsRaw.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            PartOrderLineItem(
                partId = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                category = map["category"] as? String ?: "",
                unitPrice = (map["price"] as? Number)?.toDouble() ?: 0.0,
                quantity = (map["quantity"] as? Number)?.toInt()?.coerceAtLeast(1) ?: 1,
            )
        }
        return PartOrder(
            id = doc.id,
            buyerId = doc.getString("buyerId").orEmpty(),
            buyerEmail = doc.getString("buyerEmail").orEmpty(),
            shopId = doc.getString("shopId").orEmpty(),
            shopName = doc.getString("shopName").orEmpty(),
            shopOwnerId = doc.getString("shopOwnerId").orEmpty(),
            items = items,
            totalPrice = doc.getDouble("totalPrice") ?: 0.0,
            paymentMethod = doc.getString("paymentMethod")?.let { raw ->
                runCatching { PaymentMethod.valueOf(raw) }.getOrNull()
            },
            deliveryPhone = doc.getString("deliveryPhone").orEmpty(),
            deliveryAddress = doc.getString("deliveryAddress").orEmpty(),
            deliveryLatitude = doc.getDouble("deliveryLatitude"),
            deliveryLongitude = doc.getDouble("deliveryLongitude"),
            status = status,
            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }

    fun serviceBookingToMap(booking: ServiceBookingOrder): Map<String, Any?> = mapOf(
        "buyerId" to booking.buyerId,
        "buyerEmail" to booking.buyerEmail,
        "shopId" to booking.shopId,
        "shopName" to booking.shopName,
        "shopOwnerId" to booking.shopOwnerId,
        "serviceId" to booking.serviceId,
        "serviceName" to booking.serviceName,
        "vehicleNote" to booking.vehicleNote,
        "preferredDate" to booking.preferredDate,
        "price" to booking.price,
        "status" to booking.status.name,
        "createdAt" to Timestamp(Date(booking.createdAtMillis)),
    )

    fun serviceBookingFromDocument(doc: DocumentSnapshot): ServiceBookingOrder? {
        if (!doc.exists()) return null
        val status = runCatching {
            OrderStatus.valueOf(doc.getString("status") ?: OrderStatus.PENDING.name)
        }.getOrElse { OrderStatus.PENDING }
        return ServiceBookingOrder(
            id = doc.id,
            buyerId = doc.getString("buyerId").orEmpty(),
            buyerEmail = doc.getString("buyerEmail").orEmpty(),
            shopId = doc.getString("shopId").orEmpty(),
            shopName = doc.getString("shopName").orEmpty(),
            shopOwnerId = doc.getString("shopOwnerId").orEmpty(),
            serviceId = doc.getString("serviceId").orEmpty(),
            serviceName = doc.getString("serviceName").orEmpty(),
            vehicleNote = doc.getString("vehicleNote").orEmpty(),
            preferredDate = doc.getString("preferredDate").orEmpty(),
            price = doc.getDouble("price") ?: 0.0,
            status = status,
            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        )
    }
}

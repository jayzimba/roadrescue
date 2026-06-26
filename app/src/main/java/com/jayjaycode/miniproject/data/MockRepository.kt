package com.jayjaycode.miniproject.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

object MockRepository {

    private const val BIDDING_DURATION_SECONDS = 120

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var biddingJob: Job? = null

    private val _activeRequest = MutableStateFlow<BreakdownRequest?>(null)
    val activeRequest: Flow<BreakdownRequest?> = _activeRequest.asStateFlow()

    private val _bids = MutableStateFlow<List<MechanicBid>>(emptyList())
    val bids: Flow<List<MechanicBid>> = _bids.asStateFlow()

    private val _biddingSecondsLeft = MutableStateFlow(0)
    val biddingSecondsLeft: Flow<Int> = _biddingSecondsLeft.asStateFlow()

    private val _acceptedJob = MutableStateFlow<ActiveJob?>(null)
    val acceptedJob: Flow<ActiveJob?> = _acceptedJob.asStateFlow()

    private val _cart = MutableStateFlow<List<SparePart>>(emptyList())
    val cart: Flow<List<SparePart>> = _cart.asStateFlow()

    val biddingDurationSeconds: Int get() = BIDDING_DURATION_SECONDS

    val onlineShops: List<MechanicShop> = listOf(
        MechanicShop("s1", "QuickFix Auto", 4.8f, 312, true, listOf("Towing", "Engine", "Electrical"), 2.1),
        MechanicShop("s2", "Highway Heroes", 4.6f, 189, true, listOf("Towing", "Tyres", "Battery"), 3.4),
        MechanicShop("s3", "Metro Mechanics", 4.9f, 521, true, listOf("Diagnostics", "Brakes", "AC"), 1.2),
        MechanicShop("s4", "Night Owl Garage", 4.4f, 97, false, listOf("Towing", "Transmission"), 5.0),
    )

    val spareParts: List<SparePart> = listOf(
        SparePart("p1", "Brake Pads (Front)", "Brakes", 650.00, "AutoParts Pro", "New", listOf("Toyota", "Honda"), true),
        SparePart("p2", "12V Car Battery", "Electrical", 1450.00, "PowerCell Ltd", "New", listOf("Universal"), true),
        SparePart("p3", "Oil Filter", "Engine", 185.00, "FilterMax", "New", listOf("Ford", "Chevrolet"), true),
        SparePart("p4", "Alternator (Refurb)", "Electrical", 2800.00, "ReParts Co", "Refurbished", listOf("BMW", "Mercedes"), true),
        SparePart("p5", "Spark Plugs (Set of 4)", "Engine", 420.00, "Ignite Parts", "New", listOf("Universal"), false),
        SparePart("p6", "Windshield Wipers", "Exterior", 280.00, "ClearView", "New", listOf("Universal"), true),
    )

    val servicePackages: List<ServicePackage> = listOf(
        ServicePackage("sv1", "Basic Service", "Oil change, filter check, fluid top-up", 60, 1350.00, listOf("Oil change", "Filter inspection", "Tyre pressure")),
        ServicePackage("sv2", "Full Service", "Comprehensive inspection and maintenance", 120, 3500.00, listOf("Everything in Basic", "Brake check", "Battery test", "AC check")),
        ServicePackage("sv3", "Pre-Trip Inspection", "Safety check before long journeys", 45, 950.00, listOf("Lights", "Tyres", "Fluids", "Brakes")),
    )

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
        val request = BreakdownRequest(
            id = UUID.randomUUID().toString().take(8),
            type = type,
            vehicle = vehicle,
            problemDescription = problemDescription,
            damageDescription = damageDescription,
            locationLabel = locationLabel,
            latitude = latitude,
            longitude = longitude,
            photoUris = photoUris,
            status = RequestStatus.BIDDING,
        )
        _activeRequest.value = request
        _bids.value = emptyList()
        _acceptedJob.value = null
        _biddingSecondsLeft.value = BIDDING_DURATION_SECONDS
        biddingJob?.cancel()
        biddingJob = repositoryScope.launch { startBiddingSimulation() }
        return request
    }

    private suspend fun startBiddingSimulation() {
        val onlineOnly = onlineShops.filter { it.isOnline }
        var elapsed = 0
        while (elapsed < BIDDING_DURATION_SECONDS && _acceptedJob.value == null) {
            delay(3000)
            elapsed += 3
            _biddingSecondsLeft.value = (BIDDING_DURATION_SECONDS - elapsed).coerceAtLeast(0)

            if (_bids.value.size < onlineOnly.size && Random.nextFloat() > 0.3f) {
                val shop = onlineOnly[_bids.value.size]
                val newBid = MechanicBid(
                    id = UUID.randomUUID().toString().take(6),
                    shopId = shop.id,
                    shopName = shop.name,
                    shopRating = shop.rating,
                    distanceKm = shop.distanceKm + Random.nextDouble(-0.5, 0.5),
                    etaMinutes = (shop.distanceKm * 4).toInt() + Random.nextInt(5, 15),
                    price = when (_activeRequest.value?.type) {
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
                _bids.update { (it + newBid).sortedBy { bid -> bid.price } }
            }
        }
        if (_acceptedJob.value == null && _bids.value.isNotEmpty()) {
            _biddingSecondsLeft.value = 0
        }
    }

    fun acceptBid(bid: MechanicBid) {
        val request = _activeRequest.value ?: return
        val shop = onlineShops.find { it.id == bid.shopId } ?: return
        biddingJob?.cancel()
        biddingJob = null
        _acceptedJob.value = ActiveJob(
            request = request.copy(status = RequestStatus.ACCEPTED),
            acceptedBid = bid,
            mechanicShop = shop,
        )
        _activeRequest.value = request.copy(status = RequestStatus.ACCEPTED)
        _biddingSecondsLeft.value = 0
    }

    fun cancelRequest() {
        biddingJob?.cancel()
        biddingJob = null
        _activeRequest.value = null
        _bids.value = emptyList()
        _biddingSecondsLeft.value = 0
        _acceptedJob.value = null
    }

    fun addToCart(part: SparePart) {
        _cart.update { it + part }
    }

    fun removeFromCart(part: SparePart) {
        _cart.update { current -> current.filter { it.id != part.id } }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }
}

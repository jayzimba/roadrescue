package com.jayjaycode.miniproject.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.ActiveJob
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.data.RequestStatus
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.RescueRepository
import com.jayjaycode.miniproject.data.VehicleInfo
import com.jayjaycode.miniproject.data.firebase.FirestoreConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RescueViewModel(
    private val repository: RescueRepository = RescueRepository.instance,
) : ViewModel() {

    val activeRequest: StateFlow<BreakdownRequest?> = repository.activeRequest
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isBiddingActive: StateFlow<Boolean> = activeRequest
        .map { it?.status == RequestStatus.BIDDING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bids: StateFlow<List<MechanicBid>> = repository.bids
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val secondsLeft: StateFlow<Int> = repository.biddingSecondsLeft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val acceptedJob: StateFlow<ActiveJob?> = repository.acceptedJob
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lowestBid: StateFlow<MechanicBid?> = bids
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _biddingOverlayExpanded = MutableStateFlow(false)
    val biddingOverlayExpanded: StateFlow<Boolean> = _biddingOverlayExpanded.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _trackingProgress = MutableStateFlow(0f)
    val trackingProgress: StateFlow<Float> = _trackingProgress.asStateFlow()

    private val _remainingEta = MutableStateFlow(0)
    val remainingEta: StateFlow<Int> = _remainingEta.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.loadActiveSessionIfAny()
            repository.refreshRequestHistory()
        }
    }

    fun biddingProgressFraction(request: BreakdownRequest?, secondsLeft: Int): Float {
        if (request == null) return 0f
        val totalMs = (request.biddingEndsAtMillis - request.createdAtMillis).coerceAtLeast(1L)
        val remainingMs = (secondsLeft * 1000L).coerceIn(0L, totalMs)
        return remainingMs.toFloat() / totalMs.toFloat()
    }

    fun setBiddingOverlayExpanded(expanded: Boolean) {
        _biddingOverlayExpanded.value = expanded
    }

    fun toggleBiddingOverlay() {
        _biddingOverlayExpanded.value = !_biddingOverlayExpanded.value
    }

    fun clearSubmitError() {
        _submitError.value = null
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun submitRequest(
        type: RequestType,
        vehicle: VehicleInfo,
        problem: String,
        damage: String,
        location: String,
        latitude: Double,
        longitude: Double,
        photoUris: List<Uri>,
        context: Context,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitError.value = null
            runCatching {
                repository.submitBreakdownRequest(
                    type = type,
                    vehicle = vehicle,
                    problemDescription = problem,
                    damageDescription = damage,
                    locationLabel = location,
                    latitude = latitude,
                    longitude = longitude,
                    photoUris = photoUris,
                    context = context,
                )
            }.onSuccess {
                _isSubmitting.value = false
                _biddingOverlayExpanded.value = true
                onSuccess()
            }.onFailure { error ->
                _isSubmitting.value = false
                _submitError.value = error.localizedMessage ?: "Failed to submit request"
            }
        }
    }

    fun acceptBid(bid: MechanicBid) {
        viewModelScope.launch {
            _actionError.value = null
            runCatching { repository.acceptBid(bid) }
                .onSuccess {
                    _biddingOverlayExpanded.value = false
                    startTrackingSimulation(bid.etaMinutes)
                }
                .onFailure { _actionError.value = it.localizedMessage ?: "Could not accept bid" }
        }
    }

    fun acceptLowestBid() {
        lowestBid.value?.let { acceptBid(it) }
    }

    fun extendBiddingTime() {
        viewModelScope.launch {
            _actionError.value = null
            runCatching {
                repository.extendBiddingTime(FirestoreConstants.BIDDING_EXTENSION_SECONDS)
            }.onFailure {
                _actionError.value = it.localizedMessage ?: "Could not extend time"
            }
        }
    }

    fun setAutoAcceptLowestBid(enabled: Boolean) {
        viewModelScope.launch {
            _actionError.value = null
            runCatching { repository.setAutoAcceptLowestBid(enabled) }
                .onFailure {
                    _actionError.value = it.localizedMessage ?: "Could not update preference"
                }
        }
    }

    private fun startTrackingSimulation(totalEtaMinutes: Int) {
        viewModelScope.launch {
            _remainingEta.value = totalEtaMinutes
            _trackingProgress.value = 0f
            val steps = (totalEtaMinutes * 2).coerceAtLeast(10)
            repeat(steps) {
                delay(2000)
                _trackingProgress.value = ((it + 1).toFloat() / steps).coerceAtMost(1f)
                _remainingEta.value = ((1f - _trackingProgress.value) * totalEtaMinutes).toInt().coerceAtLeast(0)
            }
            _trackingProgress.value = 1f
            _remainingEta.value = 0
        }
    }

    fun cancelRequest(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.cancelRequest()
            _biddingOverlayExpanded.value = false
            _trackingProgress.value = 0f
            _remainingEta.value = 0
            onDone()
        }
    }

    fun completeJobAndClear() {
        viewModelScope.launch {
            repository.completeActiveJob()
            _trackingProgress.value = 0f
            _remainingEta.value = 0
        }
    }
}

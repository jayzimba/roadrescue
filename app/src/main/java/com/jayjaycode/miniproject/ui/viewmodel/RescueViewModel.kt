package com.jayjaycode.miniproject.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.ActiveJob
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.RescueRepository
import com.jayjaycode.miniproject.data.VehicleInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RescueViewModel(
    private val repository: RescueRepository = RescueRepository.instance,
) : ViewModel() {

    val bids: StateFlow<List<MechanicBid>> = repository.bids
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val secondsLeft: StateFlow<Int> = repository.biddingSecondsLeft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val acceptedJob: StateFlow<ActiveJob?> = repository.acceptedJob
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val biddingDuration = repository.biddingDurationSeconds

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _trackingProgress = MutableStateFlow(0f)
    val trackingProgress: StateFlow<Float> = _trackingProgress.asStateFlow()

    private val _remainingEta = MutableStateFlow(0)
    val remainingEta: StateFlow<Int> = _remainingEta.asStateFlow()

    init {
        viewModelScope.launch {
            repository.loadActiveSessionIfAny()
            repository.refreshRequestHistory()
        }
    }

    fun clearSubmitError() {
        _submitError.value = null
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
                    photoUris = photoUris.map { it.toString() },
                )
            }.onSuccess {
                _isSubmitting.value = false
                onSuccess()
            }.onFailure { error ->
                _isSubmitting.value = false
                _submitError.value = error.localizedMessage ?: "Failed to submit request"
            }
        }
    }

    fun acceptBid(bid: MechanicBid) {
        viewModelScope.launch {
            runCatching { repository.acceptBid(bid) }
                .onSuccess { startTrackingSimulation(bid.etaMinutes) }
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

    fun cancelRequest() {
        viewModelScope.launch {
            repository.cancelRequest()
            _trackingProgress.value = 0f
            _remainingEta.value = 0
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

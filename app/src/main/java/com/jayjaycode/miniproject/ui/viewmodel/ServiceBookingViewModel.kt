package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.ServicePackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceBookingViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val services: StateFlow<List<ServicePackage>> = repository.observeServiceListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    private val _bookingError = MutableStateFlow<String?>(null)
    val bookingError: StateFlow<String?> = _bookingError.asStateFlow()

    fun clearBookingError() {
        _bookingError.value = null
    }

    fun bookService(
        service: ServicePackage,
        vehicleNote: String,
        preferredDate: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isBooking.value = true
            _bookingError.value = null
            try {
                repository.createServiceBooking(service, vehicleNote, preferredDate)
                onSuccess()
            } catch (e: Exception) {
                _bookingError.value = e.localizedMessage ?: "Booking failed"
            } finally {
                _isBooking.value = false
            }
        }
    }
}

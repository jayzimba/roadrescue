package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.ServiceBookingOrder
import com.jayjaycode.miniproject.ui.screens.BuyerOrderListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MyOrdersViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val partOrders: StateFlow<List<PartOrder>> = flow {
        val uid = repository.currentUserIdOrNull() ?: return@flow
        repository.observeMyPartOrders(uid).collect { emit(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceBookings: StateFlow<List<ServiceBookingOrder>> = flow {
        val uid = repository.currentUserIdOrNull() ?: return@flow
        repository.observeMyServiceBookings(uid).collect { emit(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<BuyerOrderListItem>> = combine(partOrders, serviceBookings) { parts, bookings ->
        val partItems = parts.map { BuyerOrderListItem.Part(it) }
        val bookingItems = bookings.map { BuyerOrderListItem.Service(it) }
        (partItems + bookingItems).sortedByDescending { it.createdAtMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class PartOrderDetailViewModel(
    orderId: String,
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val order: StateFlow<PartOrder?> = repository.observePartOrder(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(orderId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PartOrderDetailViewModel(orderId) as T
        }
    }
}

class ServiceBookingDetailViewModel(
    bookingId: String,
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val booking: StateFlow<ServiceBookingOrder?> = repository.observeServiceBooking(bookingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(bookingId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ServiceBookingDetailViewModel(bookingId) as T
        }
    }
}

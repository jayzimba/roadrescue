package com.jayjaycode.miniproject.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.BusinessType
import com.jayjaycode.miniproject.data.OrderStatus
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.ServiceBookingOrder
import com.jayjaycode.miniproject.data.ServicePackage
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.data.VehicleCompatibility
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val myBusiness = repository.myBusiness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val openJobs: StateFlow<List<BreakdownRequest>> = repository.observeOpenBreakdownRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providerJobs: StateFlow<List<BreakdownRequest>> = repository.myBusiness
        .flatMapLatest { business ->
            if (business == null) flowOf(emptyList())
            else repository.observeProviderJobs(business.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myParts: StateFlow<List<SparePart>> = repository.myBusiness
        .flatMapLatest { business ->
            if (business == null) flowOf(emptyList())
            else repository.observeMyPartListings(business.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myServices: StateFlow<List<ServicePackage>> = repository.myBusiness
        .flatMapLatest { business ->
            if (business == null) flowOf(emptyList())
            else repository.observeMyServiceListings(business.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingOrders: StateFlow<List<PartOrder>> = repository.myBusiness
        .flatMapLatest { business ->
            if (business == null) flowOf(emptyList())
            else repository.observeIncomingOrders(business.ownerId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingBookings: StateFlow<List<ServiceBookingOrder>> = repository.myBusiness
        .flatMapLatest { business ->
            if (business == null) flowOf(emptyList())
            else repository.observeIncomingBookings(business.ownerId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun registerBusiness(
        businessType: BusinessType,
        businessName: String,
        description: String,
        phone: String,
        address: String,
        servicesText: String,
        certificateUri: Uri,
        certificateFileName: String,
        context: Context,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val services = servicesText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                repository.registerBusiness(
                    businessType = businessType,
                    businessName = businessName,
                    description = description,
                    phone = phone,
                    address = address,
                    services = services,
                    certificateUri = certificateUri,
                    certificateFileName = certificateFileName,
                    context = context,
                )
                _successMessage.value = "Business registered successfully"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun placeBid(requestId: String, price: Double, etaMinutes: Int, message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.placeProviderBid(requestId, price, etaMinutes, message)
                _successMessage.value = "Bid submitted"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Bid failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPart(
        name: String,
        category: String,
        price: Double,
        condition: String,
        compatibleVehicles: List<VehicleCompatibility>,
        photoUris: List<Uri>,
        paymentMethods: Set<PaymentMethod>,
        context: Context,
        quantity: Int? = null,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.addPartListing(
                    name = name,
                    category = category,
                    price = price,
                    condition = condition,
                    compatibleVehicles = compatibleVehicles,
                    photoUris = photoUris,
                    paymentMethods = paymentMethods.toList(),
                    context = context,
                    quantity = quantity,
                )
                _successMessage.value = "Part listed"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not list part"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addService(
        name: String,
        description: String,
        durationMinutes: Int,
        price: Double,
        includesText: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val includes = includesText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                repository.addServiceListing(name, description, durationMinutes, price, includes)
                _successMessage.value = "Service listed"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not list service"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePartStock(partId: String, inStock: Boolean) {
        viewModelScope.launch {
            try {
                repository.togglePartStock(partId, inStock)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                repository.updateBookingStatus(bookingId, status)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }
}

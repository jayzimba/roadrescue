package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.SparePart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketplaceViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val parts: StateFlow<List<SparePart>> = repository.observeMarketplaceParts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPhone: StateFlow<String> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        .let { profileFlow ->
            MutableStateFlow("")
            // use map - simpler to collect in composable from repository
        }

    private val _cart = MutableStateFlow<List<SparePart>>(emptyList())
    val cart: StateFlow<List<SparePart>> = _cart.asStateFlow()

    private val _isCheckingOut = MutableStateFlow(false)
    val isCheckingOut: StateFlow<Boolean> = _isCheckingOut.asStateFlow()

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<PartOrder?>(null)
    val lastPlacedOrder: StateFlow<PartOrder?> = _lastPlacedOrder.asStateFlow()

    init {
        repository.startObservingProfile()
    }

    fun clearCheckoutError() {
        _checkoutError.value = null
    }

    fun clearLastPlacedOrder() {
        _lastPlacedOrder.value = null
    }

    fun addToCart(part: SparePart) {
        if (_cart.value.any { it.id == part.id }) return
        if (_cart.value.isNotEmpty() && _cart.value.first().shopId != part.shopId) {
            _checkoutError.value = "Your cart has items from another shop. Checkout or clear your cart first."
            return
        }
        _cart.update { it + part }
    }

    fun removeFromCart(part: SparePart) {
        _cart.update { current -> current.filter { it.id != part.id } }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun availablePaymentMethods(): List<PaymentMethod> {
        val items = _cart.value
        if (items.isEmpty()) return emptyList()
        val methodSets = items.map { it.paymentMethods.toSet() }
        val shared = methodSets.reduce { acc, methods -> acc.intersect(methods) }
        return if (shared.isNotEmpty()) {
            PaymentMethod.entries.filter { it in shared }
        } else {
            items.flatMap { it.paymentMethods }.distinct()
        }
    }

    fun checkout(
        paymentMethod: PaymentMethod,
        deliveryPhone: String,
        deliveryAddress: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isCheckingOut.value = true
            _checkoutError.value = null
            try {
                val items = cart.value
                if (items.isEmpty()) error("Your cart is empty")
                if (deliveryPhone.isBlank()) error("Enter a contact phone number")
                if (deliveryAddress.isBlank()) error("Enter a delivery address or pickup location")
                if (paymentMethod !in availablePaymentMethods()) {
                    error("Selected payment method is not available for all items in your cart")
                }
                val order = repository.createPartOrder(
                    items = items,
                    paymentMethod = paymentMethod,
                    deliveryPhone = deliveryPhone,
                    deliveryAddress = deliveryAddress,
                )
                _lastPlacedOrder.value = order
                clearCart()
                onSuccess()
            } catch (e: Exception) {
                _checkoutError.value = e.localizedMessage ?: "Checkout failed"
            } finally {
                _isCheckingOut.value = false
            }
        }
    }
}

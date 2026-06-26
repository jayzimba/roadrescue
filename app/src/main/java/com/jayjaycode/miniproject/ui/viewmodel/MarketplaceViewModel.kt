package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.CartLineItem
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.data.SparePartStock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketplaceViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    init {
        repository.startObservingProfile()
        viewModelScope.launch {
            parts.collect { refreshCommittedQuantities(it) }
        }
    }

    val parts: StateFlow<List<SparePart>> = repository.observeMarketplaceParts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPhone: StateFlow<String> = repository.userProfile
        .map { it?.phone.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _cart = MutableStateFlow<List<CartLineItem>>(emptyList())
    val cart: StateFlow<List<CartLineItem>> = _cart.asStateFlow()

    private val _committedQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    val committedQuantities: StateFlow<Map<String, Int>> = _committedQuantities.asStateFlow()

    private val _isCheckingOut = MutableStateFlow(false)
    val isCheckingOut: StateFlow<Boolean> = _isCheckingOut.asStateFlow()

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<PartOrder?>(null)
    val lastPlacedOrder: StateFlow<PartOrder?> = _lastPlacedOrder.asStateFlow()

    val cartItemCount: StateFlow<Int> = cart
        .map { lines -> lines.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartTotal: StateFlow<Double> = cart
        .map { lines -> lines.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun clearCheckoutError() {
        _checkoutError.value = null
    }

    fun clearLastPlacedOrder() {
        _lastPlacedOrder.value = null
    }

    fun committedFor(partId: String): Int = _committedQuantities.value[partId] ?: 0

    fun availableQuantity(part: SparePart): Int? =
        SparePartStock.availableQuantity(part.quantity, committedFor(part.id))

    fun maxSelectableQuantity(part: SparePart): Int? {
        val listedMax = SparePartStock.maxSelectable(part.quantity, committedFor(part.id))
        return listedMax
    }

    fun isPurchasable(part: SparePart): Boolean =
        SparePartStock.isPurchasable(part.quantity, committedFor(part.id), part.inStock)

    fun availabilityLabel(part: SparePart): String =
        SparePartStock.availabilityLabel(part.quantity, committedFor(part.id), part.inStock)

    fun cartQuantityFor(partId: String): Int =
        _cart.value.find { it.part.id == partId }?.quantity ?: 0

    fun addToCart(part: SparePart, quantity: Int = 1) {
        if (quantity < 1) return
        if (!isPurchasable(part)) {
            _checkoutError.value = "${part.name} is not available"
            return
        }
        val maxQty = maxSelectableQuantity(part)
        val existingQty = cartQuantityFor(part.id)
        val newTotal = existingQty + quantity
        if (maxQty != null && newTotal > maxQty) {
            _checkoutError.value = "Only $maxQty of ${part.name} available"
            return
        }
        if (_cart.value.isNotEmpty() && _cart.value.first().part.shopId != part.shopId) {
            _checkoutError.value = "Your cart has items from another shop. Checkout or clear your cart first."
            return
        }
        _checkoutError.value = null
        _cart.update { current ->
            val existing = current.find { it.part.id == part.id }
            if (existing != null) {
                current.map {
                    if (it.part.id == part.id) it.copy(quantity = newTotal) else it
                }
            } else {
                current + CartLineItem(part = part, quantity = quantity)
            }
        }
    }

    fun updateCartQuantity(partId: String, quantity: Int) {
        if (quantity < 1) {
            removeFromCart(partId)
            return
        }
        val line = _cart.value.find { it.part.id == partId } ?: return
        val latestPart = parts.value.find { it.id == partId } ?: line.part
        val maxQty = maxSelectableQuantity(latestPart)
        if (maxQty != null && quantity > maxQty) {
            _checkoutError.value = "Only $maxQty of ${latestPart.name} available"
            return
        }
        if (!SparePartStock.canFulfill(latestPart.quantity, committedFor(partId), quantity)) {
            _checkoutError.value = "Not enough stock for ${latestPart.name}"
            return
        }
        _checkoutError.value = null
        _cart.update { current ->
            current.map {
                if (it.part.id == partId) it.copy(part = latestPart, quantity = quantity) else it
            }
        }
    }

    fun removeFromCart(partId: String) {
        _cart.update { current -> current.filter { it.part.id != partId } }
    }

    fun removeFromCart(part: SparePart) = removeFromCart(part.id)

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun availablePaymentMethods(): List<PaymentMethod> {
        val items = _cart.value
        if (items.isEmpty()) return emptyList()
        val methodSets = items.map { it.part.paymentMethods.toSet() }
        val shared = methodSets.reduce { acc, methods -> acc.intersect(methods) }
        return if (shared.isNotEmpty()) {
            PaymentMethod.entries.filter { it in shared }
        } else {
            items.flatMap { it.part.paymentMethods }.distinct()
        }
    }

    fun checkout(
        paymentMethod: PaymentMethod,
        deliveryPhone: String,
        deliveryAddress: String,
        deliveryLatitude: Double,
        deliveryLongitude: Double,
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
                if (!deliveryLatitude.isFinite() || !deliveryLongitude.isFinite()) {
                    error("Pin your delivery location on the map")
                }
                if (paymentMethod !in availablePaymentMethods()) {
                    error("Selected payment method is not available for all items in your cart")
                }
                items.forEach { line ->
                    if (!isPurchasable(line.part)) {
                        error("${line.part.name} is no longer available")
                    }
                    val maxQty = maxSelectableQuantity(line.part)
                    if (maxQty != null && line.quantity > maxQty) {
                        error("Only $maxQty of ${line.part.name} available")
                    }
                }
                val order = repository.createPartOrder(
                    items = items,
                    paymentMethod = paymentMethod,
                    deliveryPhone = deliveryPhone,
                    deliveryAddress = deliveryAddress,
                    deliveryLatitude = deliveryLatitude,
                    deliveryLongitude = deliveryLongitude,
                )
                _lastPlacedOrder.value = order
                clearCart()
                refreshCommittedQuantities(parts.value)
                onSuccess()
            } catch (e: Exception) {
                _checkoutError.value = e.localizedMessage ?: "Checkout failed"
            } finally {
                _isCheckingOut.value = false
            }
        }
    }

    private suspend fun refreshCommittedQuantities(currentParts: List<SparePart>) {
        val shopIds = currentParts.map { it.shopId }.filter { it.isNotBlank() }.distinct()
        if (shopIds.isEmpty()) {
            _committedQuantities.value = emptyMap()
            return
        }
        val merged = mutableMapOf<String, Int>()
        shopIds.forEach { shopId ->
            merged.putAll(repository.getCommittedQuantitiesForShop(shopId))
        }
        _committedQuantities.value = merged
        clampCartToAvailability()
    }

    private fun clampCartToAvailability() {
        _cart.update { lines ->
            lines.mapNotNull { line ->
                val latest = parts.value.find { it.id == line.part.id } ?: line.part
                if (!isPurchasable(latest)) return@mapNotNull null
                val maxQty = maxSelectableQuantity(latest) ?: return@mapNotNull line.copy(part = latest)
                if (maxQty <= 0) return@mapNotNull null
                line.copy(part = latest, quantity = line.quantity.coerceAtMost(maxQty))
            }
        }
    }
}

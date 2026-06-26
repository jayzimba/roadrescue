package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.MockRepository
import com.jayjaycode.miniproject.data.SparePart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MarketplaceViewModel : ViewModel() {

    val parts = MockRepository.spareParts

    val cart: StateFlow<List<SparePart>> = MockRepository.cart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCart(part: SparePart) = MockRepository.addToCart(part)

    fun removeFromCart(part: SparePart) = MockRepository.removeFromCart(part)

    fun clearCart() = MockRepository.clearCart()
}

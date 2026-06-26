package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.MechanicShop
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    val registeredShops: StateFlow<List<MechanicShop>> = repository.observeRegisteredShops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

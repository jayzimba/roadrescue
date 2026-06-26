package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.RescueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BreakdownRequestDetailViewModel(
    requestId: String,
    private val repository: RescueRepository = RescueRepository.instance,
) : ViewModel() {

    val request: StateFlow<BreakdownRequest?> = repository.observeBreakdownRequest(requestId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(requestId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BreakdownRequestDetailViewModel(requestId) as T
            }
    }
}

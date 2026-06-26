package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.RescueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RequestHistoryViewModel(
    private val repository: RescueRepository = RescueRepository.instance,
) : ViewModel() {

    val history: StateFlow<List<BreakdownRequest>> = repository.requestHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading: StateFlow<Boolean> = repository.isLoadingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun refresh() {
        viewModelScope.launch {
            repository.refreshRequestHistory()
        }
    }
}

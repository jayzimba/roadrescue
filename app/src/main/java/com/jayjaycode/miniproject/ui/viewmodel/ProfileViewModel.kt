package com.jayjaycode.miniproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayjaycode.miniproject.data.BusinessProfile
import com.jayjaycode.miniproject.data.BusinessRepository
import com.jayjaycode.miniproject.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: BusinessRepository = BusinessRepository.instance,
) : ViewModel() {

    init {
        repository.startObservingProfile()
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val myBusiness: StateFlow<BusinessProfile?> = repository.myBusiness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun updatePhone(phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.updatePhone(phone)
                _successMessage.value = "Phone number updated"
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Update failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                repository.setOnlineStatus(isOnline)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Could not update status"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Keep observing while app is open; cleared only on sign out via AuthViewModel
    }
}

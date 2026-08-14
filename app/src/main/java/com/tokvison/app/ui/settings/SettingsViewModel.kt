package com.tokvison.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokvison.app.core.auth.AuthRepository
import com.tokvison.app.core.auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.state

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

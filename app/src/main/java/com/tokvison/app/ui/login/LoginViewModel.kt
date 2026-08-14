package com.tokvison.app.ui.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokvison.app.core.auth.AuthRepository
import com.tokvison.app.core.auth.AuthState
import com.tokvison.app.core.auth.PendingLogin
import com.tokvison.app.core.auth.TikTokAuthConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.state
    val pendingLogin: PendingLogin = authRepository.beginLogin()

    /**
     * Called from the WebView's navigation interceptor for every URL it's about to load.
     * Returns true when the URL was TikTok's redirect back to us (regardless of outcome),
     * telling the caller to cancel that navigation instead of letting the WebView load it.
     */
    fun tryHandleRedirect(url: String): Boolean {
        if (!url.startsWith(TikTokAuthConfig.redirectUri)) return false

        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")

        when {
            code != null && state != null -> viewModelScope.launch {
                authRepository.completeLogin(code, state)
            }
            error != null -> authRepository.reportAuthorizationError(
                uri.getQueryParameter("error_description") ?: "TikTok denegó el acceso.",
            )
            else -> authRepository.reportAuthorizationError("Respuesta de TikTok inesperada.")
        }
        return true
    }

    fun cancel() {
        authRepository.cancelLogin()
    }
}

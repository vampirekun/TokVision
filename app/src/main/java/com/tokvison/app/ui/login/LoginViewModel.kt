package com.tokvison.app.ui.login

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokvison.app.core.auth.AuthRepository
import com.tokvison.app.core.auth.AuthState
import com.tokvison.app.core.auth.PendingLogin
import com.tokvison.app.core.auth.TikTokAuthConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val configurationBlockReason: () -> String? = TikTokAuthConfig::directTokenExchangeBlockReason,
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.state

    var pendingLogin: PendingLogin? by mutableStateOf(null)
        private set

    /** Called every time the full-screen login overlay enters composition. */
    fun startLogin() {
        if (authRepository.state.value is AuthState.LoggedIn) return

        val blockReason = configurationBlockReason()
        if (blockReason != null) {
            pendingLogin = null
            authRepository.cancelLogin()
            authRepository.reportAuthorizationError(blockReason)
            return
        }

        pendingLogin = authRepository.beginLogin()
    }

    fun canRetry(): Boolean = configurationBlockReason() == null

    fun retry() = startLogin()

    /**
     * Called from the WebView navigation interceptor for every URL it's about to load.
     * Returns true when the URL was TikTok's redirect back to us, regardless of outcome,
     * telling the caller to cancel that navigation instead of letting the WebView load it.
     */
    fun tryHandleRedirect(url: String): Boolean {
        if (!matchesOAuthRedirect(TikTokAuthConfig.redirectUri, url)) return false

        val pending = pendingLogin ?: return true
        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")

        when {
            code != null && error == null && state == pending.state -> {
                pendingLogin = null
                viewModelScope.launch {
                    authRepository.completeLogin(code, state)
                }
            }
            error != null && code == null && state == pending.state -> {
                pendingLogin = null
                authRepository.reportAuthorizationError(
                    uri.getQueryParameter("error_description") ?: "TikTok denegó el acceso.",
                )
            }
            else -> {
                pendingLogin = null
                authRepository.reportAuthorizationError("Respuesta de TikTok inesperada o no válida.")
            }
        }
        return true
    }

    fun cancel() {
        pendingLogin = null
        authRepository.cancelLogin()
    }
}

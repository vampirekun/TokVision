package com.tokvison.app.core.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class LoggedIn(val openId: String) : AuthState
    data class Error(val message: String) : AuthState
}

/** The authorization URL to open in the login WebView, plus the values needed to validate
 * and complete the flow once TikTok redirects back with a `code`. */
data class PendingLogin(
    val authorizationUrl: String,
    val codeVerifier: String,
    val state: String,
)

/**
 * Single source of truth for "is the user logged in". Owns the PKCE dance, the token
 * exchange/refresh calls, and secure persistence — nothing else in the app touches tokens
 * directly.
 */
class AuthRepository(
    context: Context,
    private val api: TikTokAuthApi = TikTokAuthApi(),
    private val tokenStore: SecureTokenStore = SecureTokenStore(context),
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // Kept only in memory for the lifetime of a single login attempt — never persisted.
    private var pendingLogin: PendingLogin? = null

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val session = tokenStore.load()
        _state.value = if (session != null) AuthState.LoggedIn(session.openId) else AuthState.LoggedOut
    }

    /** Step 1: call before showing the login WebView. */
    fun beginLogin(): PendingLogin {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)
        val state = PkceUtil.generateState()

        val url = buildString {
            append(TikTokAuthConfig.AUTHORIZE_URL)
            append("?client_key=").append(encode(TikTokAuthConfig.clientKey))
            append("&response_type=code")
            append("&scope=").append(encode(TikTokAuthConfig.SCOPES))
            append("&redirect_uri=").append(encode(TikTokAuthConfig.redirectUri))
            append("&state=").append(encode(state))
            append("&code_challenge=").append(encode(codeChallenge))
            append("&code_challenge_method=S256")
        }

        return PendingLogin(url, codeVerifier, state).also { pendingLogin = it }
    }

    /** Step 2: call once the WebView is intercepted redirecting to [TikTokAuthConfig.redirectUri]. */
    suspend fun completeLogin(receivedCode: String, receivedState: String): Result<Unit> {
        val pending = pendingLogin
        pendingLogin = null

        if (pending == null || pending.state != receivedState) {
            _state.value = AuthState.Error("La sesión de login expiró o no es válida. Intenta de nuevo.")
            return Result.failure(IllegalStateException("State mismatch or missing pending login"))
        }

        return try {
            val response = api.exchangeCodeForToken(receivedCode, pending.codeVerifier)
            applyTokenResponse(response)
        } catch (t: Throwable) {
            _state.value = AuthState.Error("No se pudo conectar con TikTok. Revisa tu conexión e intenta de nuevo.")
            Result.failure(t)
        }
    }

    fun cancelLogin() {
        pendingLogin = null
    }

    /** Call when TikTok redirects back with an `error` query param (e.g. the user denied
     * consent) instead of a `code` — surfaces it the same way a network failure would. */
    fun reportAuthorizationError(message: String) {
        pendingLogin = null
        _state.value = AuthState.Error(message)
    }

    /** Called before any future authenticated API call (Display API, in Fase 3). Refreshes
     * the access token transparently if it's expired; logs the user out if the refresh
     * token itself is no longer valid. */
    suspend fun ensureFreshAccessToken(): String? {
        val session = tokenStore.load() ?: return null
        val now = System.currentTimeMillis()
        if (now < session.accessTokenExpiresAtMillis) {
            return session.accessToken
        }
        return try {
            val response = api.refreshAccessToken(session.refreshToken)
            if (applyTokenResponse(response).isSuccess) tokenStore.load()?.accessToken else null
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun logout() {
        tokenStore.clear()
        _state.value = AuthState.LoggedOut
    }

    private fun applyTokenResponse(response: TikTokTokenResponse): Result<Unit> {
        if (!response.isSuccess) {
            val message = response.errorDescription ?: response.error ?: "TikTok rechazó la solicitud de acceso."
            _state.value = AuthState.Error(message)
            return Result.failure(IllegalStateException(message))
        }
        val session = TikTokSession(
            openId = response.openId.orEmpty(),
            accessToken = response.accessToken.orEmpty(),
            refreshToken = response.refreshToken.orEmpty(),
            accessTokenExpiresAtMillis = System.currentTimeMillis() + (response.expiresInSeconds ?: 0L) * 1000L,
        )
        tokenStore.save(session)
        _state.value = AuthState.LoggedIn(session.openId)
        return Result.success(Unit)
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")
}

package com.tokvison.app.core.auth


/**
 * Everything a future backend/API integration needs to know about the TikTok Login Kit app
 * registration. Values come from [com.tokvison.app.BuildConfig]. The client secret is present
 * only in local debug builds; release builds intentionally receive an empty value.
 */
object TikTokAuthConfig {
    const val AUTHORIZE_URL = "https://www.tiktok.com/v2/auth/authorize/"
    const val TOKEN_URL = "https://open.tiktokapis.com/v2/oauth/token/"

    const val SCOPES = "user.info.basic"

    const val RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE =
        "El inicio de sesión está deshabilitado en esta compilación. Falta un backend HTTPS para intercambiar y renovar tokens; no se enviará el Client secret desde Android."
    const val DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE =
        "Configura TIKTOK_CLIENT_SECRET en secrets.properties para pruebas locales. No distribuyas este APK."
    const val DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE =
        "Configura TIKTOK_CLIENT_KEY y TIKTOK_REDIRECT_URI válidos en secrets.properties para pruebas locales."

    private const val CLIENT_SECRET_PLACEHOLDER = "CHANGE_ME"

    val clientKey: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_KEY
    val clientSecret: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_SECRET
    val redirectUri: String get() = com.tokvison.app.BuildConfig.TIKTOK_REDIRECT_URI

    fun directTokenExchangeBlockReason(): String? =
        directTokenExchangeBlockReason(
            isDebugBuild = com.tokvison.app.BuildConfig.DEBUG,
            clientKey = clientKey,
            secret = clientSecret,
            redirectUri = redirectUri,
        )

    fun canUseDirectTokenExchange(): Boolean = directTokenExchangeBlockReason() == null

    internal fun directTokenExchangeBlockReason(
        isDebugBuild: Boolean,
        clientKey: String,
        secret: String,
        redirectUri: String,
    ): String? = when {
        !isDebugBuild -> RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE
        clientKey.isBlank() || clientKey.trim() == CLIENT_SECRET_PLACEHOLDER ->
            DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE
        !isValidRedirectUri(redirectUri) -> DEBUG_CLIENT_CONFIGURATION_REQUIRED_MESSAGE
        secret.isBlank() || secret.trim() == CLIENT_SECRET_PLACEHOLDER ->
            DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE
        else -> null
    }

    private fun isValidRedirectUri(value: String): Boolean {
        if (value.isBlank() || value.contains("CHANGE-ME", ignoreCase = true) ||
            value.contains("CHANGE_ME", ignoreCase = true)
        ) return false

        val uri = runCatching { java.net.URI(value.trim()) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() &&
            uri.rawUserInfo == null &&
            uri.rawQuery == null &&
            uri.rawFragment == null
    }

    /** True only for a complete local debug configuration; release builds require a backend broker. */
    fun isConfigured(): Boolean = directTokenExchangeBlockReason() == null
}

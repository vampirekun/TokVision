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

    private const val CLIENT_SECRET_PLACEHOLDER = "CHANGE_ME"

    val clientKey: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_KEY
    val clientSecret: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_SECRET
    val redirectUri: String get() = com.tokvison.app.BuildConfig.TIKTOK_REDIRECT_URI

    fun directTokenExchangeBlockReason(): String? =
        directTokenExchangeBlockReason(com.tokvison.app.BuildConfig.DEBUG, clientSecret)

    fun canUseDirectTokenExchange(): Boolean = directTokenExchangeBlockReason() == null

    internal fun directTokenExchangeBlockReason(isDebugBuild: Boolean, secret: String): String? =
        when {
            !isDebugBuild -> RELEASE_TOKEN_BROKER_REQUIRED_MESSAGE
            secret.isBlank() || secret == CLIENT_SECRET_PLACEHOLDER -> DEBUG_CLIENT_SECRET_REQUIRED_MESSAGE
            else -> null
        }

    /** True only for a configured local debug build; release builds require a backend broker. */
    fun isConfigured(): Boolean =
        clientKey != CLIENT_SECRET_PLACEHOLDER &&
            !redirectUri.contains("CHANGE-ME") &&
            canUseDirectTokenExchange()
}

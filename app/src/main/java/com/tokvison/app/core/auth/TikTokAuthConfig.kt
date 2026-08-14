package com.tokvison.app.core.auth

/**
 * Everything a future backend/API integration needs to know about the TikTok Login Kit app
 * registration. Values come from [com.tokvison.app.BuildConfig], which in turn are populated
 * from `secrets.properties` (git-ignored) at build time — see `secrets.properties.example`
 * and BUILD.md for how to obtain real credentials.
 */
object TikTokAuthConfig {
    const val AUTHORIZE_URL = "https://www.tiktok.com/v2/auth/authorize/"
    const val TOKEN_URL = "https://open.tiktokapis.com/v2/oauth/token/"

    // user.info.basic: profile (open_id, avatar, display name). video.list: the user's own
    // published videos. Nothing broader is requested — TokVision doesn't need more scopes
    // for what it can legitimately show (see KNOWN_LIMITATIONS.md).
    const val SCOPES = "user.info.basic,video.list"

    val clientKey: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_KEY
    val clientSecret: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_SECRET
    val redirectUri: String get() = com.tokvison.app.BuildConfig.TIKTOK_REDIRECT_URI

    /** True once real credentials have replaced the placeholders from `secrets.properties.example`. */
    fun isConfigured(): Boolean =
        clientKey != "CHANGE_ME" && clientSecret != "CHANGE_ME" && !redirectUri.contains("CHANGE-ME")
}

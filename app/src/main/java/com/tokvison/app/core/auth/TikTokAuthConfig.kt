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

    // Request only the identity scope needed by the current OAuth/session flow.
    // video.list is deferred until the Display API integration actually ships.
    const val SCOPES = "user.info.basic"

    val clientKey: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_KEY
    val clientSecret: String get() = com.tokvison.app.BuildConfig.TIKTOK_CLIENT_SECRET
    val redirectUri: String get() = com.tokvison.app.BuildConfig.TIKTOK_REDIRECT_URI

    /** True once real credentials have replaced the placeholders from `secrets.properties.example`. */
    fun isConfigured(): Boolean =
        clientKey != "CHANGE_ME" && clientSecret != "CHANGE_ME" && !redirectUri.contains("CHANGE-ME")
}

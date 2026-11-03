package com.tokvison.app.ui.login

import java.net.URI

/**
 * Matches a TikTok OAuth callback against the registered redirect URI.
 *
 * The callback may add OAuth query parameters, but it must keep the exact HTTPS
 * authority and raw path. Prefix matching is unsafe because a URL such as
 * /callback.attacker also starts with /callback.
 */
internal fun matchesOAuthRedirect(configuredRedirectUri: String, candidateUrl: String): Boolean {
    val configured = runCatching { URI(configuredRedirectUri) }.getOrNull() ?: return false
    val candidate = runCatching { URI(candidateUrl) }.getOrNull() ?: return false

    if (!configured.isAbsolute || configured.isOpaque || !candidate.isAbsolute || candidate.isOpaque) {
        return false
    }
    if (!configured.scheme.equals("https", ignoreCase = true)) return false
    if (!candidate.scheme.equals(configured.scheme, ignoreCase = true)) return false
    if (configured.host.isNullOrBlank() || candidate.host.isNullOrBlank()) return false
    if (configured.rawUserInfo != null || candidate.rawUserInfo != null) return false
    if (configured.rawQuery != null || configured.rawFragment != null) return false
    if (candidate.rawFragment != null) return false

    // Authority comparison preserves the registered host/port while allowing host
    // casing differences. Raw-path comparison rejects prefix and encoding variants.
    if (!configured.rawAuthority.equals(candidate.rawAuthority, ignoreCase = true)) return false
    return configured.rawPath == candidate.rawPath
}

package com.tokvison.app.ui.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OAuthRedirectMatcherTest {
    private val redirectUri = "https://auth.example.com/callback"

    @Test
    fun acceptsRegisteredCallbackWithOAuthQueryParameters() {
        assertTrue(
            matchesOAuthRedirect(
                redirectUri,
                "https://auth.example.com/callback?code=synthetic-code&state=synthetic-state",
            ),
        )
    }

    @Test
    fun acceptsHostCaseDifferencesButKeepsTheRegisteredAuthority() {
        assertTrue(
            matchesOAuthRedirect(
                redirectUri,
                "https://AUTH.EXAMPLE.COM/callback?error=access_denied",
            ),
        )
    }

    @Test
    fun rejectsAPathThatOnlySharesTheRedirectPrefix() {
        assertFalse(
            matchesOAuthRedirect(
                redirectUri,
                "https://auth.example.com/callback.attacker?code=synthetic-code&state=synthetic-state",
            ),
        )
    }

    @Test
    fun rejectsNestedCallbackPaths() {
        assertFalse(
            matchesOAuthRedirect(
                redirectUri,
                "https://auth.example.com/callback/attacker?code=synthetic-code&state=synthetic-state",
            ),
        )
    }

    @Test
    fun rejectsDifferentHostsSchemesAndPorts() {
        assertFalse(matchesOAuthRedirect(redirectUri, "https://auth.example.com.attacker/callback?code=x&state=y"))
        assertFalse(matchesOAuthRedirect(redirectUri, "http://auth.example.com/callback?code=x&state=y"))
        assertFalse(matchesOAuthRedirect(redirectUri, "https://auth.example.com:8443/callback?code=x&state=y"))
    }

    @Test
    fun rejectsUserInfoAndFragments() {
        assertFalse(matchesOAuthRedirect(redirectUri, "https://attacker@auth.example.com/callback?code=x&state=y"))
        assertFalse(matchesOAuthRedirect(redirectUri, "https://auth.example.com/callback?code=x&state=y#fragment"))
    }

    @Test
    fun rejectsConfiguredRedirectUrisThatAlreadyContainQueryOrFragment() {
        assertFalse(matchesOAuthRedirect("$redirectUri?fixed=value", "$redirectUri?fixed=value&code=x&state=y"))
        assertFalse(matchesOAuthRedirect("$redirectUri#fragment", "$redirectUri?code=x&state=y"))
    }

    @Test
    fun rejectsMalformedAndRelativeUrls() {
        assertFalse(matchesOAuthRedirect(redirectUri, "not a URL"))
        assertFalse(matchesOAuthRedirect(redirectUri, "/callback?code=x&state=y"))
        assertFalse(matchesOAuthRedirect("not a URL", "https://auth.example.com/callback?code=x&state=y"))
    }

    @Test
    fun allowsOnlyAbsoluteHttpsWebViewNavigations() {
        assertTrue(isSecureOAuthWebViewNavigation("https://www.tiktok.com/v2/auth/authorize/"))
        assertTrue(isSecureOAuthWebViewNavigation("https://verify.tiktok.com/captcha"))
        assertTrue(isSecureOAuthWebViewNavigation("https://challenge.example.net/captcha"))
        assertTrue(isSecureOAuthWebViewNavigation("https://www.tiktok.com:443/v2/auth/authorize/"))

        assertFalse(isSecureOAuthWebViewNavigation("http://www.tiktok.com/v2/auth/authorize/"))
        assertFalse(isSecureOAuthWebViewNavigation("file:///sdcard/private.html"))
        assertFalse(isSecureOAuthWebViewNavigation("content://com.example.provider/private"))
        assertFalse(isSecureOAuthWebViewNavigation("intent://login#Intent;scheme=tiktok;end"))
        assertFalse(isSecureOAuthWebViewNavigation("javascript:alert(1)"))
        assertFalse(isSecureOAuthWebViewNavigation("https://user@www.tiktok.com/login"))
        assertFalse(isSecureOAuthWebViewNavigation("https://www.tiktok.com:8443/login"))
        assertFalse(isSecureOAuthWebViewNavigation("/relative/login"))
    }

}

package com.tokvison.app.core.auth

import android.webkit.CookieManager
import android.webkit.WebStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Clears the app's embedded OAuth WebView session after local logout.
 *
 * TokVision currently uses its WebView only for TikTok authorization, so removing the
 * app-wide WebView cookies and HTML5 storage prevents a later user of the TV from
 * inheriting the previous account's provider session.
 */
internal class AndroidWebViewSessionCleaner : WebViewSessionCleaner {
    override suspend fun clear() = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies {
                    try {
                        WebStorage.getInstance().deleteAllData()
                        cookieManager.flush()
                        if (continuation.isActive) continuation.resume(Unit)
                    } catch (failure: Throwable) {
                        if (continuation.isActive) continuation.resumeWithException(failure)
                    }
                }
            } catch (failure: Throwable) {
                if (continuation.isActive) continuation.resumeWithException(failure)
            }
        }
    }
}

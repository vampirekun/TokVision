package com.tokvison.app.ui.login

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tokvison.app.core.auth.AuthState

/**
 * Login happens entirely in a WebView on the TV itself — no companion device, no backend.
 * TikTok's authorize page isn't D-pad-aware, but Android's WebView forwards D-pad key events
 * into the page's own focus/click handling, so arrow keys + OK/select still work for
 * navigating TikTok's login form (see TokVision.md §5 for why this approach was chosen).
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, onFinished: () -> Unit) {
    val authState by viewModel.authState.collectAsState()
    var isPageLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            viewModel.cancel()
            onFinished()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            onFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val error = (authState as? AuthState.Error)?.message
        if (error != null) {
            LoginErrorContent(message = error, onRetry = onFinished)
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        isFocusable = true
                        isFocusableInTouchMode = true
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            @Deprecated("Deprecated in Java", ReplaceWith(""))
                            @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return url != null && viewModel.tryHandleRedirect(url)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest,
                            ): Boolean = viewModel.tryHandleRedirect(request.url.toString())

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isPageLoading = false
                            }
                        }
                        webViewRef = this
                        loadUrl(viewModel.pendingLogin.authorizationUrl)
                        requestFocus()
                    }
                },
            )
            if (isPageLoading) {
                LoginLoadingContent()
            }
        }
    }
}

@Composable
private fun LoginLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Cargando inicio de sesión de TikTok…", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LoginErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(text = "No se pudo iniciar sesión", style = MaterialTheme.typography.headlineSmall)
            Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Surface(
                onClick = onRetry,
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(text = "Volver", modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            }
        }
    }
}

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
 * TikTok's authorize page isn't D-pad-aware out of the box: browsers treat arrow keys as
 * page scroll, not focus movement, so a small spatial-navigation script is injected on
 * every page load to translate D-pad arrows into focus changes between visible focusable
 * elements (links, buttons, inputs) and to draw a visible focus outline. OK/select and
 * Back still work natively (Enter/click and WebView.goBack()).
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
                                view?.evaluateJavascript(dPadSpatialNavigationScript, null)
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

/**
 * Injected after every page load. Web pages don't natively support D-pad spatial
 * navigation (arrow keys scroll by default), so this finds the nearest focusable
 * element in the pressed direction and focuses it, and forces a visible outline since
 * TikTok's own focus styles aren't tuned for a 10-foot UI. Re-injecting on every
 * `onPageFinished` (idempotent via the `__tokvisionSpatialNavInstalled` guard) handles
 * the multiple redirects the OAuth flow goes through.
 */
private const val dPadSpatialNavigationScript = """
(function() {
  if (window.__tokvisionSpatialNavInstalled) return;
  window.__tokvisionSpatialNavInstalled = true;

  function isVisible(el) {
    if (!el) return false;
    var rect = el.getBoundingClientRect();
    if (rect.width === 0 && rect.height === 0) return false;
    var style = window.getComputedStyle(el);
    if (style.visibility === 'hidden' || style.display === 'none' || style.opacity === '0') return false;
    return rect.bottom > 0 && rect.right > 0 && rect.top < window.innerHeight && rect.left < window.innerWidth;
  }

  function focusableElements() {
    // Real <a>/<button>/<input>… plus anything with an ARIA role (TikTok's own tab
    // switcher, e.g. "Teléfono"/"Correo electrónico", is typically a <div role="tab">
    // with no tabindex — force one so it becomes reachable and focusable).
    var selector = 'a[href], button, input, select, textarea, [tabindex], [role]';
    var list = Array.prototype.slice.call(document.querySelectorAll(selector));

    // Fallback for plain, ARIA-less clickable rows (seen in TikTok's identity-verification
    // widget: a bare <div class="item-xxxx"> with a React onClick and zero accessibility
    // attributes — no role, no tabindex, not even cursor:pointer). Matched generically by
    // common list/row/option naming, not by TikTok's specific hashed class names, so this
    // also helps on other equally inaccessible sites. Only the outer-most matching
    // ancestor is kept (skip a node if its parent already matches) to avoid picking a
    // nested text/icon wrapper instead of the actual clickable row.
    var rowPattern = /(^|[-_ ])(item|row|option|choice|card)([-_ ]|\d|$)/i;
    var rowCandidates = Array.prototype.slice.call(document.querySelectorAll('div, li, span')).filter(function(el) {
      var cls = (el.className && el.className.toString()) || '';
      if (!rowPattern.test(cls)) return false;
      var rect = el.getBoundingClientRect();
      if (rect.width < 40 || rect.height < 16) return false;
      var parentCls = (el.parentElement && el.parentElement.className && el.parentElement.className.toString()) || '';
      return !rowPattern.test(parentCls);
    });

    var combined = list.concat(rowCandidates.filter(function(el) { return list.indexOf(el) === -1; }));
    combined.forEach(function(el) {
      if (!el.hasAttribute('tabindex') && el.tabIndex === -1) {
        el.setAttribute('tabindex', '0');
      }
    });
    return combined.filter(function(el) {
      return isVisible(el) && !el.disabled && el.tabIndex !== -1;
    });
  }

  function currentFocused() {
    var active = document.activeElement;
    return (active && active !== document.body && isVisible(active)) ? active : null;
  }

  function centerOf(el) {
    var r = el.getBoundingClientRect();
    return { x: r.left + r.width / 2, y: r.top + r.height / 2 };
  }

  function moveFocus(direction) {
    var elements = focusableElements();
    if (elements.length === 0) return;
    var current = currentFocused();
    if (!current) { elements[0].focus(); return; }
    var origin = centerOf(current);
    var best = null;
    var bestScore = Infinity;
    elements.forEach(function(el) {
      if (el === current) return;
      var p = centerOf(el);
      var dx = p.x - origin.x;
      var dy = p.y - origin.y;
      var matchesDirection =
        (direction === 'down' && dy > 4) ||
        (direction === 'up' && dy < -4) ||
        (direction === 'right' && dx > 4) ||
        (direction === 'left' && dx < -4);
      if (!matchesDirection) return;
      var primary = (direction === 'up' || direction === 'down') ? Math.abs(dy) : Math.abs(dx);
      var secondary = (direction === 'up' || direction === 'down') ? Math.abs(dx) : Math.abs(dy);
      var score = primary + secondary * 2;
      if (score < bestScore) { bestScore = score; best = el; }
    });
    if (best) {
      best.focus();
      best.scrollIntoView({ block: 'center', inline: 'center', behavior: 'smooth' });
    }
  }

  document.addEventListener('keydown', function(e) {
    if (e.key === 'ArrowDown') { moveFocus('down'); e.preventDefault(); }
    else if (e.key === 'ArrowUp') { moveFocus('up'); e.preventDefault(); }
    else if (e.key === 'ArrowLeft') { moveFocus('left'); e.preventDefault(); }
    else if (e.key === 'ArrowRight') { moveFocus('right'); e.preventDefault(); }
    else if (e.key === 'Enter') {
      var active = document.activeElement;
      if (!active) return;
      var tag = active.tagName;
      var type = (active.getAttribute('type') || '').toLowerCase();
      var isTextEntry = tag === 'TEXTAREA' ||
        (tag === 'INPUT' && ['text', 'tel', 'email', 'password', 'search', 'number', 'url'].indexOf(type) !== -1);
      var isNativelyClickable = tag === 'A' || tag === 'BUTTON' ||
        (tag === 'INPUT' && (type === 'submit' || type === 'button' || type === 'checkbox' || type === 'radio'));
      // Skip synthetic click while typing (Enter in a text field shouldn't "click" it);
      // native <a>/<button>/<input> already get a click from the browser on Enter, so
      // this only covers ARIA-role custom elements (tabs, custom buttons, etc.).
      if (!isTextEntry && !isNativelyClickable) {
        active.click();
      }
    }
  }, true);

  // WebView/Chromium + React controlled inputs sometimes fail to move the caret to the
  // end after each keystroke coming from a D-pad/remote, so typed digits appear to
  // insert in reverse order. Force the caret to the end after every input event, once
  // React's own re-render has settled.
  document.addEventListener('input', function(e) {
    var el = e.target;
    if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA')) {
      setTimeout(function() {
        try { el.setSelectionRange(el.value.length, el.value.length); } catch (err) {}
      }, 0);
    }
  }, true);

  var style = document.createElement('style');
  style.textContent = ':focus { outline: 3px solid #3ddc97 !important; outline-offset: 2px !important; }';
  document.head.appendChild(style);

  var initial = focusableElements()[0];
  if (initial) initial.focus();
})();
"""

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

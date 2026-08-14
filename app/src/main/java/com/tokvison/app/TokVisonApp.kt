package com.tokvison.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tokvison.app.ui.home.HomeScreen
import com.tokvison.app.ui.login.LoginScreen
import com.tokvison.app.ui.login.LoginViewModel
import com.tokvison.app.ui.navigation.TokVisonDestination
import com.tokvison.app.ui.navigation.TokVisonNavRail
import com.tokvison.app.ui.settings.SettingsScreen
import com.tokvison.app.ui.theme.TokVisonTheme

/**
 * Root composable: persistent nav rail on the left + the selected screen on the
 * right. Plain state (no navigation-graph library) is enough for these destinations;
 * this gets promoted to androidx.navigation once Player/Profile land. Login is a
 * separate full-screen overlay (no nav rail) rather than a rail destination — it's a
 * one-shot flow, not a place you navigate back and forth to.
 */
@Composable
fun TokVisonApp() {
    TokVisonTheme {
        var destination by remember { mutableStateOf(TokVisonDestination.Home) }
        var showLogin by remember { mutableStateOf(false) }

        if (showLogin) {
            val application = LocalContext.current.applicationContext as TokVisonApplication
            val loginViewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { LoginViewModel(application.authRepository) }
                },
            )
            LoginScreen(viewModel = loginViewModel, onFinished = { showLogin = false })
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                TokVisonNavRail(
                    selected = destination,
                    onSelect = { destination = it },
                )
                when (destination) {
                    TokVisonDestination.Home -> HomeScreen(modifier = Modifier.weight(1f).fillMaxSize())
                    TokVisonDestination.Settings -> SettingsScreen(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onRequestLogin = { showLogin = true },
                    )
                }
            }
        }
    }
}

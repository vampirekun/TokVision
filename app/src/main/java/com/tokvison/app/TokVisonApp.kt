package com.tokvison.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tokvison.app.ui.home.HomeScreen
import com.tokvison.app.ui.navigation.TokVisonDestination
import com.tokvison.app.ui.navigation.TokVisonNavRail
import com.tokvison.app.ui.settings.SettingsScreen
import com.tokvison.app.ui.theme.TokVisonTheme

/**
 * Root composable: persistent nav rail on the left + the selected screen on the
 * right. Plain state (no navigation-graph library) is enough for two destinations;
 * this gets promoted to androidx.navigation once Login/Player/Profile land.
 */
@Composable
fun TokVisonApp() {
    TokVisonTheme {
        var destination by remember { mutableStateOf(TokVisonDestination.Home) }

        Row(modifier = Modifier.fillMaxSize()) {
            TokVisonNavRail(
                selected = destination,
                onSelect = { destination = it },
            )
            when (destination) {
                TokVisonDestination.Home -> HomeScreen(modifier = Modifier.weight(1f).fillMaxSize())
                TokVisonDestination.Settings -> SettingsScreen(modifier = Modifier.weight(1f).fillMaxSize())
            }
        }
    }
}

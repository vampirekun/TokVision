package com.tokvison.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tokvison.app.BuildConfig
import com.tokvison.app.TokVisonApplication
import com.tokvison.app.core.auth.AuthState

private data class SettingRow(
    val label: String,
    val value: String,
    val onClick: () -> Unit = {},
)

private data class SettingSection(
    val title: String,
    val rows: List<SettingRow>,
)

/**
 * Everything here is either fully local (no TikTok API involved) or an inert stub
 * clearly labeled "próximamente". Nothing simulates functionality that isn't real yet.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, onRequestLogin: () -> Unit) {
    val application = LocalContext.current.applicationContext as TokVisonApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(application.authRepository) }
        },
    )
    val authState by viewModel.authState.collectAsState()

    val accountRow = when (val state = authState) {
        is AuthState.LoggedIn -> SettingRow(
            label = "Cerrar sesión",
            value = "Conectado (${state.openId.take(8)}…)",
            onClick = viewModel::logout,
        )
        is AuthState.Error -> SettingRow(
            label = "Iniciar sesión",
            value = state.message,
            onClick = onRequestLogin,
        )
        AuthState.LoggedOut -> SettingRow(
            label = "Iniciar sesión",
            value = "Con tu cuenta de TikTok",
            onClick = onRequestLogin,
        )
    }

    val sections = listOf(
        SettingSection(
            title = "Reproducción",
            rows = listOf(
                SettingRow("Calidad de vídeo", "Automática"),
                SettingRow("Reproducción automática", "Activada"),
            ),
        ),
        SettingSection(title = "Cuenta", rows = listOf(accountRow)),
        SettingSection(
            title = "Almacenamiento",
            rows = listOf(
                SettingRow("Limpiar caché", "0 MB"),
            ),
        ),
        SettingSection(
            title = "Acerca de",
            rows = listOf(
                SettingRow("Versión", BuildConfig.VERSION_NAME),
            ),
        ),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Text(text = "Ajustes", style = MaterialTheme.typography.displayLarge)
        }
        sections.forEach { section ->
            item {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(section.rows) { row ->
                SettingRowItem(row = row)
            }
        }
    }
}

@Composable
private fun SettingRowItem(row: SettingRow) {
    Surface(
        onClick = row.onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary)),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(text = row.label, style = MaterialTheme.typography.titleLarge)
            Text(
                text = row.value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

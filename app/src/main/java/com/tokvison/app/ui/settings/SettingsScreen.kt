package com.tokvison.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

private data class SettingRow(
    val label: String,
    val value: String,
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
fun SettingsScreen(modifier: Modifier = Modifier) {
    val sections = listOf(
        SettingSection(
            title = "Reproducción",
            rows = listOf(
                SettingRow("Calidad de vídeo", "Automática"),
                SettingRow("Reproducción automática", "Activada"),
            ),
        ),
        SettingSection(
            title = "Cuenta",
            rows = listOf(
                SettingRow("Iniciar sesión", "Próximamente"),
            ),
        ),
        SettingSection(
            title = "Almacenamiento",
            rows = listOf(
                SettingRow("Limpiar caché", "0 MB"),
            ),
        ),
        SettingSection(
            title = "Acerca de",
            rows = listOf(
                SettingRow("Versión", "0.1.0-skeleton"),
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
        onClick = { /* Stubs today; wired up as each setting becomes real. */ },
        modifier = Modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
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

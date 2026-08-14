package com.tokvison.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

private data class NavRailItem(
    val destination: TokVisonDestination,
    val label: String,
    val icon: ImageVector,
)

/**
 * Persistent left rail: always visible, always reachable with D-pad up/down, and the
 * single entry point into every screen. Deliberately just two items today (Home,
 * Settings) — matches the real, working navigation graph, not an aspirational one.
 */
@Composable
fun TokVisonNavRail(
    selected: TokVisonDestination,
    onSelect: (TokVisonDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        NavRailItem(TokVisonDestination.Home, "Inicio", Icons.Filled.Home),
        NavRailItem(TokVisonDestination.Settings, "Ajustes", Icons.Filled.Settings),
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(120.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items.forEach { item ->
            NavRailButton(
                item = item,
                isSelected = item.destination == selected,
                onClick = { onSelect(item.destination) },
            )
        }
    }
}

@Composable
private fun NavRailButton(
    item: NavRailItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .size(72.dp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = if (isSelected) {
                Border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary))
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary)),
        ),
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(imageVector = item.icon, contentDescription = item.label)
            Text(text = item.label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

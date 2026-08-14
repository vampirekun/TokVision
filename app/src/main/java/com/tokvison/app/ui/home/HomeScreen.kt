package com.tokvison.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

private data class HomeCard(
    val title: String,
    val subtitle: String,
)

/**
 * Home today only shows what actually works end to end: local placeholders for the
 * features landing in Fase 2/3 (own profile, own videos, play-by-link). No feed, no
 * search — those aren't backed by any official TikTok API (see KNOWN_LIMITATIONS.md).
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val firstCardFocusRequester = remember { FocusRequester() }
    val cards = listOf(
        HomeCard("Mi perfil", "Próximamente — vía TikTok Display API"),
        HomeCard("Mis vídeos", "Próximamente — vía TikTok Display API"),
        HomeCard("Reproducir por enlace", "Próximamente — pega un enlace de TikTok"),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "Bienvenido a TokVison",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = "Cliente ligero de TikTok para Android TV",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        ) {
            itemsIndexed(cards) { index, card ->
                val cardModifier = if (index == 0) {
                    Modifier.focusRequester(firstCardFocusRequester)
                } else {
                    Modifier
                }
                HomeFeatureCard(card = card, modifier = cardModifier)
            }
        }
    }

    LaunchedEffect(Unit) {
        firstCardFocusRequester.requestFocus()
    }
}

@Composable
private fun HomeFeatureCard(card: HomeCard, modifier: Modifier = Modifier) {
    Surface(
        onClick = { /* Not wired yet: lands with Fase 2/3. */ },
        modifier = modifier
            .width(260.dp)
            .height(150.dp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary)),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = card.title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

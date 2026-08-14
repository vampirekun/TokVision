package com.tokvison.app.ui.navigation

/**
 * Top-level destinations available today. Kept as a plain enum on purpose: this is the
 * entire navigation graph for the Fase 1 skeleton (Home, Settings). New destinations
 * (Login, Player, Profile, History/Favorites) get added here as their phases land —
 * no navigation framework restructuring needed.
 */
enum class TokVisonDestination(val route: String) {
    Home("home"),
    Settings("settings"),
}

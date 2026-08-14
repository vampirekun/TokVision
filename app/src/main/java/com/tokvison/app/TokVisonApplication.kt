package com.tokvison.app

import android.app.Application
import com.tokvison.app.core.auth.AuthRepository

/**
 * No DI framework: a single Application-scoped container is enough for the handful of
 * shared objects this app has (today, just [authRepository]). Add fields here, not a Hilt
 * module, as new shared dependencies (network client, caches) land in later phases.
 */
class TokVisonApplication : Application() {
    val authRepository: AuthRepository by lazy { AuthRepository(applicationContext) }
}

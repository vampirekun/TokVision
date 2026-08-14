import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// TikTok Login Kit credentials never get hardcoded/committed: they're read from a local,
// git-ignored properties file and injected as BuildConfig fields. Placeholders keep the
// project buildable before the developer account/app registration is complete (see BUILD.md).
val secretsProperties = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { load(it) }
    }
}
fun secret(key: String, default: String) = secretsProperties.getProperty(key, default)

android {
    namespace = "com.tokvison.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tokvison.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.0-auth"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "TIKTOK_CLIENT_KEY", "\"${secret("TIKTOK_CLIENT_KEY", "CHANGE_ME")}\"")
        buildConfigField("String", "TIKTOK_CLIENT_SECRET", "\"${secret("TIKTOK_CLIENT_SECRET", "CHANGE_ME")}\"")
        buildConfigField(
            "String",
            "TIKTOK_REDIRECT_URI",
            "\"${secret("TIKTOK_REDIRECT_URI", "https://CHANGE-ME.github.io/tokvison-oauth/callback")}\"",
        )
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.4")

    // Jetpack Compose for TV: focus/D-pad-aware Material components (Surface, Button, Card…).
    // tv-foundation's lazy-list wrappers were folded into plain Compose Foundation upstream,
    // so LazyRow/LazyColumn are used directly and tv-foundation isn't needed for the skeleton.
    implementation("androidx.tv:tv-material:1.1.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Icons only (no full Material components lib): a couple of glyphs for the nav rail.
    implementation("androidx.compose.material:material-icons-core")

    // Auth: OAuth2+PKCE against TikTok Login Kit. Ktor (Android/HttpURLConnection engine) +
    // kotlinx.serialization keep this to a couple of small, coroutines-first, reflection-free
    // libraries instead of Retrofit+Gson+RxJava.
    implementation("io.ktor:ktor-client-core:3.5.2")
    implementation("io.ktor:ktor-client-android:3.5.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Session tokens live in EncryptedSharedPreferences (Keystore-backed), never in plain prefs.
    implementation("androidx.security:security-crypto:1.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

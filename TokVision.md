# TokVision — Documento de contexto del proyecto

> Este documento es el complemento detallado al [README.md](/c:/Users/owner/Development/TokVision/README.md). Está escrito para que **cualquier agente o desarrollador que retome el proyecto** entienda, sin tener que releer todo el historial de la sesión: qué se está construyendo, por qué se tomaron ciertas decisiones, qué falta, y qué caminos futuros existen para el mayor reto del proyecto (el feed "For You"). Mantenlo actualizado al final de cada fase.

---

## 1. Qué es TokVision y qué NO es

TokVision es un cliente **ligero, nativo, para Android TV/Google TV** que reproduce la experiencia de la antigua app "TikTok para TV", pero construido **exclusivamente sobre las APIs oficiales de TikTok for Developers**.

No es:
- Un clon completo de TikTok con feed algorítmico infinito (esa API no existe públicamente — ver sección 4).
- Una app usando endpoints internos/privados de TikTok o scraping. Se decidió explícitamente **no** hacer esto (ver sección 4.3).
- Una PWA o WebView genérica del sitio móvil de TikTok (aunque es una alternativa futura evaluada, ver sección 5.2).

Es:
- Un visor TV-first del **perfil propio del usuario autenticado**: su información, sus vídeos publicados, y reproducción de vídeos por enlace compartido.
- Una base de código honesta, ligera, mantenible, preparada para absorber una API de feed pública si TikTok la publica alguna vez.

## 2. Estado actual del proyecto

| Fase | Nombre | Estado |
|---|---|---|
| 0 | Research (viabilidad técnica) | ✅ Completada |
| 1 | Skeleton (proyecto Android, tema, nav D-pad, Home, Settings) | ✅ Completada |
| 2 | Autenticación (OAuth2+PKCE vía WebView en la TV) | ✅ Código completo — pendiente que el usuario registre la app en TikTok (ver [BUILD.md](/c:/Users/owner/Development/TokVision/BUILD.md)) y pruebe login real |
| 3 | Feed de vídeo propio (Display API) + reproductor Media3 | 🔜 Siguiente |
| 4 | Interacciones locales (favoritos/historial) | ⬜ Pendiente |
| 5 | Search/Discover | ❌ Omitida (no viable oficialmente, ver sección 4) |
| 6 | Optimización (profiling RAM/CPU/startup) | ⬜ Pendiente |
| 7 | QA (unit/UI/playback tests) | ⬜ Pendiente |
| 8 | Release (APK firmado) | ⬜ Pendiente |

Repositorio: [c:\Users\owner\Development\TokVision](/c:/Users/owner/Development/TokVision), Git inicializado localmente, un commit (`feat: Fase 1 skeleton...`).

## 3. Decisiones de producto ya tomadas por el usuario (no las reabras sin preguntar)

Estas tres decisiones fueron confirmadas explícitamente por el dueño del producto durante la Fase 0 y **no deben revertirse sin volver a preguntar**:

1. **Alcance oficial únicamente**: sin feed de terceros, sin búsqueda, sin scraping/reverse engineering como parte del producto principal.
2. **Login sin infraestructura externa**: WebView/Custom Tab directamente en la TV, navegable con D-pad. Sin backend propio, sin Cloudflare Worker, sin servidor intermedio.
3. **Sin overengineering**: sin Hilt, sin Clean Architecture estricta, sin RxJava. Coroutines + Flow, inyección manual, 3 capas (UI → ViewModel/UseCase → Repository).

## 4. La restricción fundamental: por qué no hay feed "For You"

### 4.1 Qué API pública existe hoy

- **Login Kit**: OAuth 2.0 + PKCE, estándar, basado en redirect. Sin device-flow nativo para TV.
- **Display API** (`/v2/user/info/`, `/v2/video/list/`, `/v2/video/query/`): perfil y vídeos **del propio usuario autenticado únicamente**. Scopes: `user.info.basic`, `video.list`.
- **Content Posting API**: publicar contenido a la cuenta autenticada (subida, no consumo).
- **Research API**: consulta de vídeos/comentarios públicos, pero **restringida a investigadores académicos verificados**, con términos que prohíben uso productivo como cliente de consumo. No es una opción viable ni legal para TokVision.

Ninguna de ellas expone un feed algorítmico de terceros, búsqueda pública, ni interacciones (like/comment/follow) sobre contenido ajeno. Detalle completo, con formato FEATURE/STATUS/REASON/ALTERNATIVE, en [KNOWN_LIMITATIONS.md](/c:/Users/owner/Development/TokVision/KNOWN_LIMITATIONS.md).

### 4.2 Qué significa esto para el producto

TokVision, con datos 100% oficiales, solo puede mostrar: perfil propio, vídeos propios, y reproducción de un vídeo si el usuario tiene su URL pública (vía oEmbed). El "feed" en TokVision hoy y en el corto plazo es, en la práctica, **la propia colección del usuario** (sus vídeos + su historial + sus favoritos locales), no un feed algorítmico de descubrimiento.

### 4.3 Módulo no oficial — descartado, pero documentado por si se reconsidera

El usuario decidió explícitamente **no** construir un módulo que dependa de endpoints internos de la app móvil de TikTok (ingeniería inversa, tokens internos, llamadas no documentadas). Motivo: viola los ToS de TikTok, es fràgil (los endpoints cambian sin aviso), arriesga el bloqueo de cuenta/IP de los usuarios, y exige mantenimiento indefinido tipo "gato y ratón". Si en el futuro se reconsidera, debe vivir en un módulo Gradle separado (`:feature-unofficial`), detrás de un flag desactivado por defecto, nunca mezclado con el código oficial.

## 5. Alternativas futuras para acercarse a un "feed For You" (evaluación honesta)

Ninguna de estas es una solución mágica; cada una tiene trade-offs reales. Se documentan para que una futura sesión de trabajo pueda evaluarlas con el usuario sin tener que repetir esta investigación.

### 5.1 Monitorizar la API oficial de TikTok for Developers

La opción más simple y de menor riesgo: revisar periódicamente [developers.tiktok.com](https://developers.tiktok.com) y su changelog en busca de un futuro "Feed API" o "Discovery API" pública. Si TikTok alguna vez la publica (algo que ha hecho para otras plataformas via acuerdos comerciales puntuales, pero no de forma pública general), la arquitectura de TokVision (capa `Repository` con interfaz) permite añadirla sin rediseñar la app. **Acción recomendada**: revisar esto al inicio de cada fase futura.

### 5.2 "TikTok Web" embebido y adaptado para TV (WebView optimizado)

**Idea**: en vez de reimplementar el feed con una API que no existe, cargar el sitio web real de TikTok (`www.tiktok.com` o `m.tiktok.com`) dentro de un `WebView` de Android, e inyectar CSS/JS propio para:
- Adaptar el layout a pantalla grande y distancia de sofá (fuentes más grandes, ocultar elementos táctiles/móviles).
- Mapear teclas D-pad (arriba/abajo/OK/atrás) a scroll vertical, play/pause y navegación, ya que el D-pad no genera eventos táctiles nativos que la web de TikTok entienda.
- Interceptar reproducción de vídeo para intentar delegarla a `ExoPlayer` nativo si es posible extraer la URL del stream (mismo enfoque que un lector de "modo lectura" de un navegador, no un endpoint privado).

**Por qué es distinta de "scraping de API privada"**: aquí no se llama a ningún endpoint interno ni se firman peticiones falsificando la app móvil — simplemente se navega la página pública tal como lo haría cualquier navegador, igual que Fire TV/Google TV a veces resuelven servicios sin app nativa mostrando su versión web. Es un enfoque mucho más defendible legal y técnicamente.

**Riesgos y contras reales (por qué no se implementó ya)**:
- **Va en contra de la prioridad #1 del proyecto (ligereza)**: un `WebView` con el motor completo de Chromium consume mucho más RAM/CPU que Compose + Media3, y contradice directamente la restricción fundamental de la sección 2 del brief original ("no quiero una app móvil escalada a TV").
- TikTok Web **no está diseñado para D-pad**; el resultado sería, en el mejor caso, una adaptación torpe, con posible scroll/foco poco fiable.
- TikTok activa medidas anti-automatización/anti-bot en la web para tráfico "no navegador típico"; un `User-Agent` o patrón de interacción atípico (control remoto en vez de touch/mouse) podría ser bloqueado o mostrar CAPTCHA, sin garantía de estabilidad.
- Sigue sin ser 100% "oficial" en el sentido de un contrato de API — TikTok podría cambiar su web (estructura HTML/CSS/JS) en cualquier momento y romper la integración, similar al riesgo del módulo no oficial de la sección 4.3, aunque con menor severidad legal.
- No hay reproducción hardware-eficiente: el vídeo dentro de un `WebView` normalmente no aprovecha `ExoPlayer`/decodificación optimizada de Android TV de la misma manera que un reproductor nativo.

**Recomendación**: si se decide explorar esto, hacerlo como **experimento aislado y opcional** (similar al módulo no oficial: un flag desactivado por defecto, en un módulo separado), nunca como el modo por defecto de la app, y medir consumo de RAM/CPU real en un dispositivo TV de gama baja antes de considerarlo viable. Requiere aprobación explícita del usuario antes de invertir tiempo en ello, dado que contradice el principio de ligereza del proyecto.

### 5.3 Curaduría propia del usuario (ya planeado, Fase 3-4)

En vez de un feed algorítmico, dejar que el propio usuario construya su "feed" pegando enlaces de TikTok desde el móvil (compartir → TokVision, o pegar manualmente), guardándolos en Favoritos/Historial locales. No sustituye al descubrimiento algorítmico, pero es 100% oficial, estable, y ya está en el roadmap (Fase 3/4).

### 5.4 Acceso Business/Partner con TikTok

TikTok ofrece acuerdos comerciales especiales (p. ej. para partners de distribución de contenido) que sí incluyen acceso a feeds/contenido de terceros bajo contrato. Esto es una **decisión de negocio, no técnica** — requeriría que el dueño del producto contacte formalmente a TikTok for Business/Partnerships. Fuera del alcance de un agente de desarrollo; se documenta solo como posibilidad a largo plazo.

### 5.5 Otras ideas de producto para el roadmap (no relacionadas con el feed)

Ideas de valor que no dependen de resolver el problema del feed, y que pueden añadirse en fases futuras sin fricción con TikTok:

- **Integración con el canal "Watch Next" de Android TV**: TokVision podría publicar los vídeos favoritos/en progreso del usuario en la fila nativa de recomendaciones del launcher de Android TV (`TvProvider`/`WatchNextPrograms`), una API 100% nativa de Android, sin relación con TikTok.
- **Google Assistant / control por voz**: comandos básicos ("reproducir", "pausa", "siguiente") vía `MediaSession`, que Android TV ya soporta de forma nativa para cualquier reproductor multimedia.
- **Perfiles múltiples locales**: permitir varias cuentas de TikTok vinculadas en la misma TV (útil en un hogar), cada una con su propio token/historial local.
- **Modo "screensaver" / ambient**: cuando la TV está inactiva, reproducir en bucle silencioso los vídeos favoritos del usuario (similar a los protectores de pantalla de Google Fotos).
- **Chromecast/cast desde el móvil**: permitir enviar un enlace de TikTok desde la app móvil oficial (compartir) directamente a TokVision en la TV vía un intent/`NSD` local, sin necesidad de backend (esto complementa la idea de "reproducir por enlace" ya planeada).
- **Recomendaciones on-device basadas en el propio historial**: un modelo ligero local (o simplemente reglas por hashtags/autor más vistos) que reordene los vídeos favoritos/historial del propio usuario — no es un feed de descubrimiento real, pero mejora la sensación de "para ti" usando solo datos que el usuario ya generó.

## 6. Arquitectura y stack (resumen — detalle completo en el historial de la Fase 0)

```
app/
 ├── ui/
 │    ├── theme/         (Color.kt, Theme.kt, Type.kt — MaterialTheme de tv-material)
 │    ├── navigation/     (TokVisonDestination.kt, TokVisonNavRail.kt)
 │    ├── home/           (HomeScreen.kt)
 │    └── settings/       (SettingsScreen.kt)
 ├── MainActivity.kt
 └── TokVisonApp.kt       (root composable: Row [NavRail | pantalla seleccionada])
```

Capas futuras (aún no creadas, se añaden según la fase):
- `core/network`: cliente HTTP (Ktor + kotlinx.serialization) para Login Kit/Display API.
- `core/auth`: OAuth2+PKCE, `EncryptedSharedPreferences` para tokens.
- `core/player`: wrapper de Media3 ExoPlayer (un solo player activo, preload mínimo del siguiente vídeo, liberación inmediata del anterior).
- `core/cache`: SQLDelight para metadata/historial/favoritos; Coil para thumbnails.

| Decisión | Elección | Motivo |
|---|---|---|
| UI | Jetpack Compose for TV (`androidx.tv:tv-material` 1.1.0) | Foco/D-pad de primera clase, más ligero que Leanback clásico (en mantenimiento mínimo) |
| Lenguaje/Build | Kotlin 2.2.21, AGP 8.13.2, Gradle 8.13 | Combinación estable y mutuamente compatible verificada por compilación real (ver nota de versiones abajo) |
| compileSdk/targetSdk | 36 | Requerido por dependencias `androidx.tv`/Compose actuales |
| minSdk | 23 | `androidx.tv:tv-foundation` exige mínimo 23 (Android 6.0); ajustado desde el 21 original tras un fallo real de merge de manifiesto |
| Networking (futuro) | Ktor + kotlinx.serialization | Ligero, coroutines-first, sin Retrofit+Gson+RxJava |
| Storage sesión (futuro) | EncryptedSharedPreferences | Tokens cifrados con Keystore, sin librerías pesadas |
| Storage metadata (futuro) | SQLDelight | SQL tipado sin reflection, arranque rápido |
| Vídeo (futuro) | Media3 ExoPlayer | Estándar de facto Android, decodificación hardware, integración con Compose |
| DI | Ninguna (constructor injection manual) | Hilt no se justifica para el tamaño de este proyecto |

### Nota importante sobre versiones de dependencias (para evitar repetir el mismo problema)

Durante la Fase 1 se descubrió que usar las **versiones absolutas más recientes** de cada librería (`androidx.core`, `compose-bom`, `androidx.tv`, etc.) de forma independiente genera conflictos de resolución en cascada: cada librería exige un `compileSdk`/AGP mínimo más alto que el anterior, escalando sin fin. La solución fue fijar un **compose-bom concreto (2026.02.00)** cuya versión de `compose-runtime`/`foundation` coincide exactamente con la que usa `androidx.tv:tv-material:1.1.0` internamente (verificado leyendo el `.pom` publicado de tv-material), y elegir el resto de versiones (`core-ktx`, `activity-compose`, `lifecycle`, `navigation-compose`) de la misma generación temporal. **Antes de subir cualquier versión de una librería en este proyecto, verifica su `.pom` en `dl.google.com/dl/android/maven2/...` para confirmar qué versión de Compose/compileSdk exige realmente**, en vez de asumir que "más nuevo es mejor".

También se descubrió que `androidx.tv:tv-foundation` **ya no contiene** `TvLazyRow`/`TvLazyColumn` (esa funcionalidad se fusionó en el `LazyRow`/`LazyColumn` estándar de Compose Foundation, que ya soporta 2D focus search para D-pad de forma nativa). Por eso el proyecto usa `androidx.compose.foundation.lazy.LazyRow` directamente y **no depende de `tv-foundation`** en absoluto — solo de `tv-material` para los componentes visuales (`Surface`, `ClickableSurfaceDefaults`, etc.).

## 7. Cómo retomar el desarrollo

1. Lee este documento y [KNOWN_LIMITATIONS.md](/c:/Users/owner/Development/TokVision/KNOWN_LIMITATIONS.md) completos antes de tocar código.
2. Verifica que el entorno tenga JDK 17, y que `local.properties` apunte al Android SDK local (`sdk.dir`) — no se versiona en git.
3. `./gradlew assembleDebug` debe compilar sin tocar nada (es el estado verificado al final de la Fase 1).
4. La Fase 2 (Autenticación) ya tiene el código completo: OAuth2+PKCE contra TikTok Login Kit, con un `WebView` navegable por D-pad dentro de la propia TV (sin backend externo). Falta el paso administrativo del usuario: registrar la app en developers.tiktok.com (ver [BUILD.md](/c:/Users/owner/Development/TokVision/BUILD.md)) y crear `secrets.properties` con las credenciales reales — sin eso, el login compila pero TikTok responde `invalid_client`.
5. La siguiente tarea pendiente es la **Fase 3 — Feed de vídeo propio + reproductor**: `core/network` (Ktor ya está en el proyecto, reutilizarlo), llamadas a `/v2/user/info/` y `/v2/video/list/` de la Display API usando `AuthRepository.ensureFreshAccessToken()`, y el wrapper de Media3 ExoPlayer.
6. No reabras las decisiones de la sección 3 sin preguntar explícitamente al usuario.
7. Cuando el feed/búsqueda vuelvan a surgir como tema, remite a la sección 5 de este documento en vez de re-investigar desde cero.

## 8. Módulo de autenticación (Fase 2) — notas de implementación

- `core/auth/PkceUtil.kt`: genera `code_verifier`/`code_challenge` (RFC 7636, S256) y el `state` anti-CSRF.
- `core/auth/TikTokAuthConfig.kt`: URLs y scopes fijos; credenciales leídas de `BuildConfig` (a su vez generadas desde `secrets.properties`, git-ignorado — ver `secrets.properties.example`).
- `core/auth/TikTokAuthApi.kt`: Ktor + kotlinx.serialization, solo dos llamadas (intercambio de código, refresh de token) contra `https://open.tiktokapis.com/v2/oauth/token/`.
- `core/auth/SecureTokenStore.kt`: `EncryptedSharedPreferences` (Keystore-backed), nunca `SharedPreferences` planas.
- `core/auth/AuthRepository.kt`: única fuente de verdad de sesión (`StateFlow<AuthState>`), orquesta PKCE + red + almacenamiento; expone `ensureFreshAccessToken()` para que las futuras llamadas a la Display API (Fase 3) obtengan un token válido sin preocuparse de refrescarlo.
- `ui/login/LoginScreen.kt`: el login ocurre en un `WebView` normal dentro de la TV — no en Custom Tabs ni en un navegador externo — porque así se puede interceptar la navegación al `redirect_uri` (`shouldOverrideUrlLoading`) sin depender de que esa página cargue nada.
- **Importante — por qué el redirect_uri es `https://` y no un esquema personalizado**: TikTok valida `redirect_uri` como una URL `https://` absoluta y estática; no acepta esquemas tipo `tokvison://`. Por eso existe [web/oauth-callback/index.html](/c:/Users/owner/Development/TokVision/web/oauth-callback/index.html), pensada para alojarse gratis en GitHub Pages — es solo para satisfacer esa validación, la lógica real vive en el WebView de la app.
- `TikTokAuthConfig.isConfigured()` permite detectar si `secrets.properties` sigue con los valores de ejemplo, útil para mostrar un aviso claro en vez de un error genérico de TikTok (pendiente de conectar en la UI si se considera útil).
- **Pendiente conocido para Fase 7 (QA)**: `PkceUtil` usa `android.util.Base64`, que no funciona en tests unitarios JVM puros sin Robolectric — al escribir tests para este módulo, usa Robolectric o inyecta un encoder abstraído.

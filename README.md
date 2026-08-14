# TokVison

Cliente ligero de TikTok para **Android TV / Google TV**, optimizado para mando a distancia (D-pad), bajo consumo de RAM/CPU y arranque rápido.

> **Estado actual: Fase 1 — Skeleton.** Proyecto Android nativo con Jetpack Compose for TV, tema, navegación D-pad y dos pantallas (Home, Ajustes). Sin red, sin login, sin reproducción todavía — eso llega en las fases siguientes. Ver [KNOWN_LIMITATIONS.md](/c:/Users/owner/Development/TokVision/KNOWN_LIMITATIONS.md) para el alcance real frente a las APIs oficiales de TikTok.

## Por qué TokVison es distinto de "TikTok en el móvil pero grande"

TikTok **no ofrece hoy una API pública** de feed algorítmico ("For You"/"Following"), búsqueda, ni likes/comentarios/follows sobre contenido de terceros. Por eso TokVison se construye exclusivamente sobre lo que la [TikTok for Developers API](https://developers.tiktok.com) permite oficialmente: perfil propio, vídeos propios, y reproducción de enlaces. Nada de scraping ni endpoints privados. El detalle completo está en [KNOWN_LIMITATIONS.md](/c:/Users/owner/Development/TokVision/KNOWN_LIMITATIONS.md).

## Requisitos

- JDK 17
- Android SDK (compileSdk 36, build-tools 36.1.0)
- Un dispositivo o caja Android TV / Google TV con **Android 6.0 (API 23) o superior**

## Compilar

```powershell
git clone <repo-url> TokVision
cd TokVision
.\gradlew.bat assembleDebug
```

El APK debug queda en `app\build\outputs\apk\debug\app-debug.apk`.

Para un build de release (minificado con R8, sin logs de debug):

```powershell
.\gradlew.bat assembleRelease
```

## Instalar en una Android TV real

1. En la TV: **Ajustes → Preferencias del dispositivo → Acerca de** → pulsa 7 veces sobre "Compilación" para activar Opciones de desarrollador.
2. **Ajustes → Preferencias del dispositivo → Opciones de desarrollador** → activa **Depuración USB/de red**.
3. Conecta por red desde tu PC:

   ```powershell
   adb connect <IP_DE_LA_TV>:5555
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

4. Desde la TV, abre **TokVison** en la fila de apps del launcher (aparece porque declara `LEANBACK_LAUNCHER`).

## Arquitectura (resumen)

UI en Jetpack Compose for TV (`androidx.tv:tv-material`), sin framework de navegación todavía (un `enum` + `when` es suficiente para 2 pantallas). Ver el reporte de viabilidad técnica en el historial de la sesión para el diseño completo (networking, auth, player, caché) que se implementará en las próximas fases.

## Stack elegido

| Área | Elección |
|---|---|
| UI | Jetpack Compose for TV (`tv-material` 1.1.0) |
| Lenguaje | Kotlin 2.2.21 |
| Build | AGP 8.13.2 + Gradle 8.13 |
| compileSdk / targetSdk | 36 |
| minSdk | 23 (Android 6.0 — requerido por `androidx.tv`) |

## Próximas fases

Fase 2 (Autenticación OAuth vía WebView en la TV) → Fase 3 (Mis vídeos + reproductor Media3) → Fase 4 (Favoritos/Historial locales) → Fase 6 (Optimización) → Fase 7 (QA) → Fase 8 (Release firmado).

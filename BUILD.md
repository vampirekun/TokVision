# BUILD.md — Compilar TokVision y registrar la app en TikTok for Developers

## 1. Compilar (sin login funcional)

El proyecto compila e instala perfectamente **sin** credenciales de TikTok — solo que el botón "Iniciar sesión" mostrará un error de TikTok (`invalid_client`) hasta que completes la sección 2.

```powershell
git clone https://github.com/vampirekun/TokVision.git
cd TokVision
.\gradlew.bat assembleDebug
```

## 2. Registrar TokVision en TikTok for Developers (paso a paso)

Ya tienes cuenta en [developers.tiktok.com](https://developers.tiktok.com), así que empieza en el paso 3.

1. Crea una cuenta de desarrollador en <https://developers.tiktok.com/signup> (ya hecho).
2. (Opcional pero recomendado) Crea una organización — puedes saltarte esto y registrar la app a tu cuenta individual.
3. Entra a <https://developers.tiktok.com/login/>.
4. Haz clic en tu icono de perfil (arriba a la derecha) → **Manage apps** (<https://developers.tiktok.com/apps>).
5. Haz clic en **Connect an app**.
6. Cuando te pida **Select the app owner**, elige tu organización (o tu cuenta individual) y confirma.
7. Rellena la información básica de la app:
   - **App name**: TokVision.
   - **Category**: Entertainment.
   - **Description**: describe brevemente qué hace (ver sugerencia abajo).
   - **Terms of Service URL**: `https://vampirekun.github.io/TokVision/legal/terms.html`
   - **Privacy Policy URL**: `https://vampirekun.github.io/TokVision/legal/privacy.html`
   - **Platforms**: marca **Web** (NO "Android" — el SDK nativo de Android depende de la app oficial de TikTok instalada en el teléfono, que no existe para Android TV; por eso TokVision usa el flujo web genérico dentro de un WebView en la propia TV. Ver [TokVision.md](/c:/Users/owner/Development/TokVision/TokVision.md) sección 5 para el detalle completo).

   Sugerencia de texto para **Description** (120 caracteres máx.): `Cliente ligero de TikTok para Android TV: ve tu perfil y tus videos con el control remoto.`

   Las páginas de Términos de Servicio y Política de Privacidad ya están escritas y publicadas en este repositorio — ver [docs/legal/terms.html](/c:/Users/owner/Development/TokVision/docs/legal/terms.html) y [docs/legal/privacy.html](/c:/Users/owner/Development/TokVision/docs/legal/privacy.html). Descritas honestamente: qué datos accede TokVision (perfil y vídeos propios vía Display API), dónde se guardan (solo en el dispositivo, tokens cifrados), y que no hay backend propio ni terceros involucrados.
8. En la sección **Products**, añade **Login Kit**.
9. Dentro de la configuración de Login Kit:
   - **Scopes**: activa `user.info.basic` y `video.list`.
   - **Redirect URI**: añade exactamente `https://vampirekun.github.io/TokVision/callback/` (publicada vía GitHub Pages desde este mismo repositorio — ver sección 3 más abajo). Debe ser `https://`, absoluta, sin parámetros ni `#`.
10. Guarda y, cuando quieras probar login real (fuera de Sandbox), sigue el flujo de **Submit your app for review** de TikTok. Mientras tanto, el modo **Sandbox** del portal permite probar el login sin pasar la revisión completa (revisa las restricciones de Sandbox en su documentación — normalmente limita el login a los usuarios que tú añadas como testers).
11. Copia el **Client key** y el **Client secret** de la sección **Credentials** de tu app.

## 3. Páginas legales y de redirect (ya publicadas vía GitHub Pages)

TikTok exige, antes de poder añadir productos, una **Terms of Service URL** y una **Privacy Policy URL**, además del `redirect_uri` de Login Kit. Las tres páginas ya están escritas y publicadas en este repositorio, alojadas gratis vía GitHub Pages (rama `main`, carpeta `/docs`):

| Página | Ruta en el repo | URL pública |
|---|---|---|
| Redirect de OAuth | [docs/callback/index.html](/c:/Users/owner/Development/TokVision/docs/callback/index.html) | `https://vampirekun.github.io/TokVision/callback/` |
| Términos de Servicio | [docs/legal/terms.html](/c:/Users/owner/Development/TokVision/docs/legal/terms.html) | `https://vampirekun.github.io/TokVision/legal/terms.html` |
| Política de Privacidad | [docs/legal/privacy.html](/c:/Users/owner/Development/TokVision/docs/legal/privacy.html) | `https://vampirekun.github.io/TokVision/legal/privacy.html` |

TokVision nunca depende de que la página de redirect *haga* nada — solo necesita existir para que TikTok la acepte al registrar la app; la lógica real ocurre dentro del WebView de la app, que intercepta esa URL antes de que termine de cargar (ver [LoginScreen.kt](/c:/Users/owner/Development/TokVision/app/src/main/java/com/tokvison/app/ui/login/LoginScreen.kt)). Las páginas de Términos/Privacidad sí son leídas por TikKok y potencialmente por los usuarios, y describen honestamente qué hace TokVision.

Si alguna vez mueves el proyecto a otro repositorio/organización, repite el mismo esquema (`/docs/callback`, `/docs/legal`) y actualiza las tres URLs en TikTok y en `secrets.properties`.

## 4. Configurar las credenciales localmente

```powershell
Copy-Item secrets.properties.example secrets.properties
notepad secrets.properties
```

Rellena:

```properties
TIKTOK_CLIENT_KEY=<tu client key>
TIKTOK_CLIENT_SECRET=<tu client secret>
TIKTOK_REDIRECT_URI=https://vampirekun.github.io/TokVision/callback/
```

`secrets.properties` está en `.gitignore` — nunca se sube al repositorio.

## 5. Recompilar y probar

```powershell
.\gradlew.bat assembleDebug
adb connect <IP_DE_LA_TV>:5555
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Desde **Ajustes → Cuenta → Iniciar sesión** en la app, deberías ver la pantalla de autorización real de TikTok dentro del WebView.

## 6. Build de release

```powershell
.\gradlew.bat assembleRelease
```

Nota: el release aún no está firmado con una keystore real (eso llega en la Fase 8 — Release); por ahora `assembleRelease` genera un APK sin firmar, útil solo para validar que R8/minificación no rompan nada.

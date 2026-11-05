# BUILD.md — Compilar TokVision y registrar la app en TikTok for Developers

## 1. Compilar (sin login funcional)

El proyecto compila sin credenciales de TikTok. Sin una configuración válida, el inicio de sesión no podrá completar el intercambio OAuth.

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
   - **Description**: Cliente para Android TV que permite iniciar y cerrar sesión con TikTok mediante Login Kit.
   - **Terms of Service URL**: `https://vampirekun.github.io/TokVision/legal/terms.html`
   - **Privacy Policy URL**: `https://vampirekun.github.io/TokVision/legal/privacy.html`
   - **Platforms**: marca **Web** para el flujo WebView actualmente implementado en Android TV. No marques Android solo por tratarse de un APK: esta implementación no integra el OpenSDK Android de TikTok.

   Las páginas legales describen el alcance actual: OAuth Login/Logout, sin Display API, sin consulta de perfiles/vídeos y sin base de datos propia. La autenticación sí intercambia solicitudes y respuestas con TikTok; consulta la política de privacidad para los datos y destinos descritos.
8. En la sección **Products**, añade **Login Kit**.
9. Dentro de la configuración de Login Kit:
   - **Scope solicitado por la versión actual**: `user.info.basic`.
   - No habilites ni solicites `video.list` todavía: la Display API y la consulta de vídeos propios no están implementadas.
   - **Redirect URI**: añade exactamente `https://vampirekun.github.io/TokVision/callback/` (publicada vía GitHub Pages desde este mismo repositorio — ver sección 3 más abajo). Debe ser `https://`, absoluta, sin parámetros ni `#`.
10. Guarda y, cuando quieras probar login real (fuera de Sandbox), sigue el flujo de **Submit your app for review** de TikTok. Mientras tanto, el modo **Sandbox** del portal permite probar el login sin pasar la revisión completa; revisa las restricciones vigentes de Sandbox y sus testers.
11. Copia el **Client key** y el **Client secret** solo para la configuración de desarrollo descrita en la sección 4. No distribuyas un APK que contenga el Client secret.

## 3. Páginas legales y de redirect (ya publicadas vía GitHub Pages)

TikTok exige, antes de poder añadir productos, una **Terms of Service URL** y una **Privacy Policy URL**, además del `redirect_uri` de Login Kit. Las tres páginas están alojadas vía GitHub Pages (rama `main`, carpeta `/docs`):

| Página | Ruta en el repo | URL pública |
|---|---|---|
| Redirect de OAuth | `docs/callback/index.html` | `https://vampirekun.github.io/TokVision/callback/` |
| Términos de Servicio | `docs/legal/terms.html` | `https://vampirekun.github.io/TokVision/legal/terms.html` |
| Política de Privacidad | `docs/legal/privacy.html` | `https://vampirekun.github.io/TokVision/legal/privacy.html` |

TokVision no depende de que la página de redirect haga nada: la lógica actual intercepta esa URL dentro del WebView. Si cambias de repositorio u organización, actualiza las tres URLs tanto en TikTok Developer Portal como en la configuración de la aplicación.

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

`secrets.properties` está en `.gitignore`, lo que evita subir ese archivo al repositorio. **Eso no convierte el Client secret en un secreto seguro dentro de una aplicación Android.**

### Bloqueo de seguridad antes de distribuir

La implementación actual lee `TIKTOK_CLIENT_SECRET` desde `secrets.properties`, lo copia a `BuildConfig.TIKTOK_CLIENT_SECRET` y lo utiliza desde `TikTokAuthApi` en el dispositivo para llamar al endpoint de tokens. Por tanto, el valor queda incorporado al APK y puede extraerse. R8/minificación, ofuscación, firma del APK y `.gitignore` no resuelven esa exposición.

PKCE protege el intercambio frente a la interceptación del código de autorización; no vuelve confidencial un Client secret empaquetado en un cliente público.

La guía oficial actual de TikTok indica que, para Android, el cliente debe enviar el código de autorización y el `code_verifier` al backend para el intercambio de tokens, y que el Client secret y el refresh token deben permanecer en el servidor. TokVision todavía no implementa ese backend: el intercambio directo desde Android es una limitación de desarrollo, no una arquitectura apta para distribuir a usuarios.

**No distribuyas APK debug ni release con un Client secret real.** Antes de una beta pública o de usar cuentas de terceros, hay que implementar y probar un backend/token broker HTTPS que realice el intercambio y gestione los refresh tokens del lado servidor. Esta documentación no implementa ese servicio ni convierte el flujo actual en seguro para producción.

## 5. Recompilar y probar localmente

```powershell
.\gradlew.bat assembleDebug
adb connect <IP_DE_LA_TV>:5555
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Las pruebas locales deben usar una cuenta de desarrollo/Sandbox. No compartas el APK generado mientras contenga un Client secret real.

Desde **Ajustes → Cuenta → Iniciar sesión** en la app, deberías ver la pantalla de autorización de TikTok dentro del WebView.

## 6. Build de release

```powershell
.\gradlew.bat assembleRelease
```

El release aún no está firmado con una keystore real. Además, aunque compile, **no es apto para distribución** mientras el intercambio de tokens y el Client secret permanezcan dentro del APK. La firma real, el backend/token broker, la gestión server-side de refresh tokens y las pruebas de logout/runtime son gates separados; ninguno debe darse por completado por el mero hecho de generar un APK.

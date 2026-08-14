# BUILD.md — Compilar TokVision y registrar la app en TikTok for Developers

## 1. Compilar (sin login funcional)

El proyecto compila e instala perfectamente **sin** credenciales de TikTok — solo que el botón "Iniciar sesión" mostrará un error de TikTok (`invalid_client`) hasta que completes la sección 2.

```powershell
git clone <repo-url> TokVision
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
   - **Nombre**: TokVision (o el que prefieras).
   - **Plataformas**: marca **Web** (NO "Android" — el SDK nativo de Android depende de la app oficial de TikTok instalada en el teléfono, que no existe para Android TV; por eso TokVision usa el flujo web genérico dentro de un WebView en la propia TV. Ver [TokVision.md](/c:/Users/owner/Development/TokVision/TokVision.md) sección 5 para el detalle completo).
8. En la sección **Products**, añade **Login Kit**.
9. Dentro de la configuración de Login Kit:
   - **Scopes**: activa `user.info.basic` y `video.list`.
   - **Redirect URI**: añade exactamente la URL que publiques desde [web/oauth-callback/index.html](/c:/Users/owner/Development/TokVision/web/oauth-callback/index.html) (ver sección 3 más abajo). Debe ser `https://`, absoluta, sin parámetros ni `#`.
10. Guarda y, cuando quieras probar login real (fuera de Sandbox), sigue el flujo de **Submit your app for review** de TikTok. Mientras tanto, el modo **Sandbox** del portal permite probar el login sin pasar la revisión completa (revisa las restricciones de Sandbox en su documentación — normalmente limita el login a los usuarios que tú añadas como testers).
11. Copia el **Client key** y el **Client secret** de la sección **Credentials** de tu app.

## 3. Publicar la página de redirect (gratis, sin backend)

TikTok exige que el `redirect_uri` sea una URL `https://` real. TokVision nunca depende de que esa página *haga* nada — solo necesita existir para que TikTok la acepte al registrar la app; la lógica real ocurre dentro del WebView de la app, que intercepta la URL antes de que termine de cargar (ver [LoginScreen.kt](/c:/Users/owner/Development/TokVision/app/src/main/java/com/tokvison/app/ui/login/LoginScreen.kt)).

**Opción recomendada: GitHub Pages (gratis)**

1. Crea un repositorio público nuevo en GitHub, por ejemplo `tokvison-oauth`.
2. Sube el archivo [web/oauth-callback/index.html](/c:/Users/owner/Development/TokVision/web/oauth-callback/index.html) de este proyecto a la ruta `callback/index.html` de ese repositorio.
3. En **Settings → Pages** del repositorio, activa GitHub Pages sirviendo desde la rama `main` (carpeta raíz).
4. Tu URL quedará como `https://<tu-usuario>.github.io/tokvison-oauth/callback/` (nota la barra final — cópiala tal cual, incluyendo o no la barra, pero usa **exactamente la misma** en TikTok y en `secrets.properties`).

## 4. Configurar las credenciales localmente

```powershell
Copy-Item secrets.properties.example secrets.properties
notepad secrets.properties
```

Rellena:

```properties
TIKTOK_CLIENT_KEY=<tu client key>
TIKTOK_CLIENT_SECRET=<tu client secret>
TIKTOK_REDIRECT_URI=https://<tu-usuario>.github.io/tokvison-oauth/callback/
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

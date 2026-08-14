# Limitaciones conocidas — TokVison

Este documento existe porque la especificación del proyecto exige explícitamente no ocultar ni simular funcionalidades que no son posibles con las APIs oficiales de TikTok. Cada fila documenta una funcionalidad de la antigua app "TikTok para TV", su estado real hoy, la razón, y la alternativa adoptada.

| FEATURE | STATUS | REASON | AVAILABLE ALTERNATIVE |
|---|---|---|---|
| Feed "For You" (algorítmico) | ❌ No implementado | Ninguna API pública de TikTok expone un feed de recomendación de contenido de terceros. La [Display API](https://developers.tiktok.com/doc/display-api-overview) solo devuelve vídeos **del propio usuario autenticado**. | Ninguna alternativa oficial. Ver sección "Módulo no oficial" abajo. |
| Feed "Following" | ❌ No implementado | Mismo motivo que el anterior. | Ninguna |
| Reproducir vídeos de **cualquier** usuario | ❌ No implementado | La Display API no expone vídeos de terceros. | Reproducción por **enlace compartido** (oEmbed público de TikTok) cuando esa fase se implemente |
| Búsqueda de vídeos / usuarios / hashtags | ❌ No implementado | No existe endpoint de búsqueda en Login Kit, Display API ni Content Posting API. La Research API sí permite búsquedas pero está restringida a investigadores académicos aprobados por TikTok, con términos que prohíben uso productivo tipo "cliente de consumo". | Ninguna alternativa oficial |
| Comentarios (leer/publicar) sobre contenido ajeno | ❌ No implementado | No expuesto por ninguna API pública de consumo general. | Ninguna |
| Like / Follow sobre contenido ajeno | ❌ No implementado | No expuesto por ninguna API pública. | Ninguna |
| Perfil propio (avatar, nombre, bio) | ✅ Planeado (Fase 3) | `GET /v2/user/info/` de la Display API, scope `user.info.basic` | — |
| Mis vídeos (los que el usuario ha publicado) | ✅ Planeado (Fase 3) | `GET /v2/video/list/` y `/v2/video/query/`, scope `video.list` | — |
| Login / Logout | ✅ Planeado (Fase 2) | OAuth 2.0 + PKCE (Login Kit), vía WebView navegable con D-pad directamente en la TV | — |
| Compartir vídeo | ⚠️ Parcial | Share Kit de TikTok es para compartir *hacia* TikTok, no *desde* TikTok hacia otra app | Deep link a la app oficial de TikTok o al navegador |
| Publicar vídeo propio | ⚠️ Fuera de alcance | Content Posting API existe y es técnicamente viable, pero no encaja con el propósito de un cliente de *visualización* para TV | No planeado |
| Favoritos / Historial | ✅ Planeado (Fase 4) | 100% local (SQLDelight), no depende de ninguna API de TikTok | — |

## Sobre el "módulo no oficial"

El usuario del proyecto decidió explícitamente **no** incluir ningún módulo que dependa de endpoints internos/no documentados de TikTok (scraping, reverse engineering de la app móvil, tokens internos), por los siguientes riesgos, que quedan documentados aquí para cualquier revisión futura de esta decisión:

- Viola los Términos de Servicio de TikTok.
- Los endpoints internos cambian sin aviso — mantenimiento tipo "gato y ratón" indefinido.
- Riesgo de bloqueo de cuenta/IP para los usuarios de la app.
- No hay garantía legal ni de estabilidad a largo plazo.

Si en el futuro se decide reconsiderar esto, debe implementarse en un módulo Gradle separado (p. ej. `:feature-unofficial`), detrás de un flag desactivado por defecto, nunca mezclado con el código que depende de la API oficial.

## Login en TV: captcha de arrastrar (drag puzzle) de TikTok

Durante las pruebas reales en un Chromecast con Google TV (agosto 2026) se confirmó que el flujo de login (WebView + navegación D-pad inyectada vía JS, ver `LoginScreen.kt`) funciona correctamente para: escribir usuario/contraseña, cambiar de pestaña "Teléfono"/"Correo", código OTP por SMS, código TOTP de app de autenticación, y elegir el método de verificación "Verifica que eres tú". Sin embargo, en algunos intentos TikTok presenta un **captcha de arrastrar** ("arrastra el deslizador para encajar en el puzzle"), que:

- Requiere un gesto de arrastre continuo (mousedown + mousemove + mouseup), no reproducible de forma fiable ni legítima con D-pad únicamente.
- Está diseñado deliberadamente para detectar automatización (analiza suavidad/velocidad del movimiento) — **no se debe intentar automatizar con JavaScript**, por las mismas razones que se descartó el módulo no oficial en la sección anterior: arriesga marcar la cuenta del usuario como sospechosa.
- El "modo trackpad" del control remoto por app de teléfono (probado en Chromecast con Google TV) **no resolvió el problema** — no genera los eventos de arrastre continuo que el widget espera.
- Es posible que aparezca con más frecuencia si el flujo de login se repite muchas veces seguidas en poco tiempo (múltiples reinstalaciones/reintentos), ya que el sistema antifraude de TikTok es sensible al riesgo de la sesión. **Usar Chrome DevTools Protocol / WebView remote debugging (`setWebContentsDebuggingEnabled`) durante una sesión de login real es sospechoso de disparar el antibot de TikTok** (su SDK `webmssdk.js` es conocido por detectar señales de automatización/depuración remota) — evitar tenerlo activo durante pruebas de login reales; solo activarlo puntualmente para diagnóstico y desactivarlo de inmediato después (como se hizo aquí).

**Alternativas legítimas pendientes de validar** (el usuario debe ser quien resuelve el captcha, nunca la app):
1. Conectar un mouse USB/Bluetooth real al dispositivo Android TV temporalmente para ese paso puntual.
2. Reintentar el login más tarde, en un solo intento tranquilo, sin depuración remota activa.
3. Si el problema persiste sistemáticamente, contactar soporte de TikTok for Developers — podría ser una señal de que la IP/dispositivo de pruebas quedó marcada temporalmente por el volumen de intentos durante el desarrollo.

## Referencias

- [TikTok for Developers — Display API Overview](https://developers.tiktok.com/doc/display-api-overview)
- [TikTok for Developers — OAuth token management](https://developers.tiktok.com/doc/oauth-user-access-token-management)
- [TikTok for Developers — Research API](https://developers.tiktok.com/doc/research-api-specs-query-videos) (acceso restringido, no usado en TokVison)

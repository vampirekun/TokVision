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

## Referencias

- [TikTok for Developers — Display API Overview](https://developers.tiktok.com/doc/display-api-overview)
- [TikTok for Developers — OAuth token management](https://developers.tiktok.com/doc/oauth-user-access-token-management)
- [TikTok for Developers — Research API](https://developers.tiktok.com/doc/research-api-specs-query-videos) (acceso restringido, no usado en TokVison)

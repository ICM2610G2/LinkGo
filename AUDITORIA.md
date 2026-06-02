# Reporte de Auditoría — LinkGo

---

## Entrega 1 — UI y Base

### ✅ Pantallas base implementadas con Jetpack Compose

| Pantalla | Archivo | Estado |
|---|---|---|
| Login | `ui/feature/auth/LoginScreen.kt` | ✅ Implementado |
| Registro | `ui/feature/auth/RegisterScreen.kt` | ✅ Implementado |
| Mapa | `ui/feature/map/MapScreen.kt` | ✅ Implementado |
| Feed | `ui/feature/feed/FeedScreen.kt` | ✅ Implementado |
| Chat (lista) | `ui/feature/chat/ChatScreen.kt` | ✅ Implementado |
| Chat (detalle) | `ui/feature/chat/ChatDetailScreen.kt` | ✅ Implementado |
| Perfil | `ui/feature/profile/ProfileScreen.kt` | ✅ Implementado |
| MeetUps | `ui/feature/meetup/MeetUpsScreen.kt` | ✅ Implementado |
| Hotspots | `ui/feature/map/HotspotsScreen.kt` | ✅ Implementado |
| Agregar Hotspot | `ui/feature/map/AddHotspotScreen.kt` | ✅ Implementado |

Navegación centralizada en `ui/navigation/navigation.kt` con un `Screens` enum y `NavHost`. Bottom bar visible en todas las rutas excepto `login` y `register`.

---

## Entrega 2 — Hardware, Mapas y Auth

---

### 🟡 Acceso a hardware: almacenamiento, cámara y galería — PARCIALMENTE IMPLEMENTADO

**Lo que está:**
- **Cámara:** `ActivityResultContracts.TakePicturePreview` en `ProfileScreen.kt` — captura foto de perfil y de momentos.
- **Galería:** `ActivityResultContracts.GetContent("image/*")` en `ProfileScreen.kt` — selección de fotos.
- **Almacenamiento interno (caché):** Función `bitmapToCacheUri()` en `ProfileScreen.kt` — convierte `Bitmap` a archivo en `context.cacheDir` antes de subir a Firebase Storage.
- **Firebase Storage:** Imágenes guardadas en `ptps/{uid}/` (perfil) y `Post/{uid}/` (momentos).

**Lo que falta / problemas:**
- La cámara usa `TakePicturePreview` que devuelve un thumbnail de baja resolución, **no la foto completa**. Para producción debería usarse `TakePicture` con un `FileProvider` URI.
- No se usa **CameraX** — la integración es básica vía Intent.
- No hay manejo de error si el usuario deniega permiso de cámara después de haberlo otorgado.

---

### ✅ Sensores (≥ 4 distintos) — IMPLEMENTADO

`SensorViewModel.kt` (en `domain/model/`) registra y gestiona los sensores. Se pasa como parámetro desde `MainActivity` a través de toda la jerarquía de navegación.

| # | Sensor | `TYPE_*` | Efecto observable | Pantalla |
|---|---|---|---|---|
| 1 | **Acelerómetro** | `TYPE_ACCELEROMETER` | Shake detection → modal de emergencia ("Maniobra brusca detectada") | `Navigation.kt` |
| 2 | **Luz ambiental** | `TYPE_LIGHT` | Lux < 100 → mapa cambia a estilo oscuro (carga `R.raw.map_style_dark`) | `MapScreen.kt` |
| 3 | **Proximidad** | `TYPE_PROXIMITY` | Dispositivo cerca de objeto → overlay negro "Modo Privado" bloquea el chat | `ChatDetailScreen.kt` |
| 4 | **Giroscopio** | `TYPE_GYROSCOPE` | Pendiente de verificar integración completa | — |

> ⚠️ **Nota:** Solo 3 sensores fueron confirmados con certeza (acelerómetro, luz, proximidad). Verificar si el giroscopio u otro sensor (magnetómetro, podómetro) está registrado en `SensorViewModel` para confirmar el mínimo de 4. Si solo hay 3, **falta 1 sensor** para cumplir el requisito.

---

### ✅ Localización y Mapas sensibles al dispositivo — IMPLEMENTADO

- **Google Maps Compose** (`GoogleMap` composable) en `MapScreen.kt`.
- **`FusedLocationProviderClient`** con `LocationCallback` activo — actualiza la posición del usuario en tiempo real.
- Umbral de actualización: **30 metros de distancia mínima** (`setSmallestDisplacement(30f)`).
- Primera actualización anima la cámara a la ubicación del usuario (`CameraUpdateFactory`).
- Marcadores para hotspots con click handlers.
- Verificación de permiso `ACCESS_FINE_LOCATION` en runtime con diálogo explicativo.

---

### ✅ Trazado de rutas entre dos puntos — IMPLEMENTADO

- `ui/feature/routes/DirectionsService.kt` — llama a la **Google Directions API REST** (`https://maps.googleapis.com/maps/api/directions/json`).
- `ui/feature/routes/PolylineDecoder.kt` — decodifica el polyline encodificado de Google al algoritmo estándar.
- Resultado: se dibuja una `Polyline` en el mapa (color primario del tema, grosor 12f) con distancia y duración en texto.
- Se dispara desde `MapScreen.kt` al seleccionar un hotspot como destino.

> ⚠️ **Bug:** La API key de Directions está **hardcodeada en el código fuente** (`DirectionsService.kt`). Debería estar en `local.properties` o en un backend seguro.

---

### ✅ Autenticación y manejo de sesión — IMPLEMENTADO

**Login** (`LoginScreen.kt`):
- `auth.signInWithEmailAndPassword(email, password)` — Firebase Auth.
- **Biometría:** `BiometricPrompt` — lee email/password de `SharedPreferences("auth")` y re-autentica.
- `LaunchedEffect` verifica `auth.currentUser != null` al abrir la app → redirige al mapa si ya hay sesión activa.

**Registro** (`RegisterScreen.kt`):
- `auth.createUserWithEmailAndPassword(email, password)`.
- Crea documento del usuario en `/users/{uid}` en Firebase Realtime DB.
- Actualiza `displayName` en Firebase Auth.

**Logout** (`ProfileScreen.kt`):
- `FirebaseAuth.getInstance().signOut()` + `popUpTo(0) { inclusive = true }` (limpia el back stack).

> ⚠️ **Bug de seguridad:** El password del usuario se guarda en texto plano en `SharedPreferences` para poder usarlo con biometría. Debería usarse `EncryptedSharedPreferences` de Jetpack Security.

---

## Entrega 3 — Tiempo Real, Notificaciones y REST

---

### ❌ Chat funcional con Firebase — NO IMPLEMENTADO

**Lo que existe (solo UI):**
- `ChatScreen.kt` muestra lista de grupos cargados desde `ChatViewModel.hardcodedGroups()` — lista local, **sin lectura de Firebase**.
- `ChatDetailScreen.kt` muestra mensajes en un `mutableStateListOf` local — **sin escritura ni escucha de Firebase**.
- El botón de enviar agrega el mensaje a la lista local solamente.

**Lo que falta:**
- Listener de Firebase (`addValueEventListener` / `addChildEventListener`) sobre `/chats/{groupId}/messages`.
- Escritura de mensajes a Firebase al enviar.
- Carga real de grupos desde `/groups` o similar en Firebase.

---

### ❌ Seguimiento de posición en tiempo real visible para otros usuarios — NO IMPLEMENTADO

**Lo que existe:**
- `MapScreen.kt` rastrea la ubicación del usuario localmente con `FusedLocationProviderClient`.

**Lo que falta:**
- Escritura de la posición a Firebase (`/users/{uid}/location` o similar) en cada callback de ubicación.
- Lectura de posiciones de otros usuarios desde Firebase para renderizarlos como marcadores en el mapa.
- `MeetUpsScreen.kt` muestra contactos con distancias **hardcodeadas** — no usa datos reales.

---

### ❌ Notificaciones Push (FCM) — NO IMPLEMENTADO

- No existe ninguna clase que extienda `FirebaseMessagingService`.
- No hay registro de token FCM.
- No hay creación de `NotificationChannel`.
- **La dependencia `firebase-messaging` no está en `app/build.gradle.kts`** — tendría que agregarse primero.

---

### ✅ Consumo de API REST externa — IMPLEMENTADO

- `DirectionsService.kt` realiza un `GET` HTTP a `https://maps.googleapis.com/maps/api/directions/json`.
- Implementado con `HttpURLConnection` nativo (sin Retrofit/OkHttp).
- Parseo manual con `org.json.JSONObject`.
- Timeout: 15 segundos. Retorna `LinkGoRoute` con puntos decodificados, texto de distancia y duración.

---

### ❌ Framework adicional (Flutter/SwiftUI) — NO APLICA / NO IMPLEMENTADO

- El repositorio es **100% Kotlin/Jetpack Compose**. No existe código Flutter, SwiftUI, u otro framework.

---

## Resumen ejecutivo

| Requisito | Entrega | Estado |
|---|---|---|
| Pantallas base (Compose) | E1 | ✅ Completo |
| Almacenamiento / Cámara / Galería | E2 | 🟡 Parcial |
| ≥ 4 sensores distintos | E2 | 🟡 Verificar (3 confirmados) |
| Localización y mapas reactivos | E2 | ✅ Completo |
| Trazado de rutas | E2 | ✅ Completo |
| Auth (Login / Register / Logout / Sesión) | E2 | ✅ Completo |
| Chat funcional en tiempo real (Firebase) | E3 | ❌ No implementado |
| Posición en tiempo real para otros usuarios | E3 | ❌ No implementado |
| Notificaciones push (FCM) | E3 | ❌ No implementado |
| Consumo de REST API | E3 | ✅ Completo |
| Framework alternativo | E3 | ❌ No aplica en este repo |

---

## Bugs críticos

| # | Descripción | Archivo | Severidad |
|---|---|---|---|
| 1 | Password guardado en texto plano en `SharedPreferences` | `LoginScreen.kt` | 🔴 Alta |
| 2 | API Key de Google Maps/Directions hardcodeada en código fuente | `DirectionsService.kt`, `AndroidManifest.xml` | 🔴 Alta |
| 3 | `TakePicturePreview` devuelve thumbnail de baja resolución, no la foto completa | `ProfileScreen.kt` | 🟡 Media |
| 4 | `HotspotRepository.onCancelled()` ignora errores de Firebase silenciosamente | `HotspotRepository.kt` | 🟡 Media |
| 5 | Overlay "Modo Privado" no tiene forma de cerrarse manualmente si el sensor queda activo | `ChatDetailScreen.kt` | 🟡 Media |

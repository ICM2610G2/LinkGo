Guía Completa: Introducción a la Computación Móvil

Institución: Pontificia Universidad Javeriana - Departamento de Ingeniería de Sistemas

Profesor: Carlos Andrés Parra (ca.parraa@javeriana.edu.co)

Tecnologías Principales: Android, Kotlin, Jetpack Compose, Firebase, Parse, Docker.

Índice Temático

Introducción al Desarrollo Móvil y Android

Fundamentos de Programación en Kotlin

Interfaces de Usuario con Jetpack Compose

Manejo de Estado y Arquitectura

Acceso a Hardware y Permisos

Localización, Sensores y Mapas

Backend Backend as a Service (BaaS): Firebase

Alternativas de Backend: Parse Platform y Docker

Servicios Adicionales: REST, Notificaciones y Background

1. Introducción al Desarrollo Móvil y Android

Para la creación de aplicaciones móviles, existen varios enfoques, cada uno con ventajas y desventajas:

Web Móvil: Sitios web adaptados al navegador del móvil.

Ventajas: Rápido de implementar, multiplataforma nativa (depende del navegador).

Desventajas: Experiencia de usuario (UX) inferior, sin acceso a hardware, requiere conexión a internet (no offline).

Híbrido (Ionic, Cordova): Tecnologías web (HTML5, Angular) empaquetadas en una app nativa.

Ventajas: Facilita la transición para desarrolladores web, multiplataforma.

Desventajas: Rendimiento inferior, UX sigue siendo similar a la web.

Nativo (Android/Java/Kotlin, iOS/Swift): Herramientas provistas por los creadores del SO.

Ventajas: Acceso total al dispositivo, máximo rendimiento, excelente UX, funciona offline.

Desventajas: Costoso, curva de aprendizaje alta, código no reutilizable entre plataformas.

Nativo Multiplataforma (Flutter/Dart, Compose Multiplatform): Frameworks de terceros que compilan a nativo.

Ventajas: Una sola base de código, alto rendimiento, acceso a casi todo el hardware.

Desventajas: Hay que aprender un nuevo lenguaje/framework, configuración manual de despliegue por plataforma.

Arquitectura de Android

Linux Kernel: Base del SO.

HAL (Hardware Abstraction Layer): Expone las capacidades de hardware.

Android Runtime (ART): Instancias donde corre cada app (Similar a la JVM).

Native C/C++ Libraries: Soporte para gráficos 2D/3D.

Java API Framework: APIs de UI, recursos, notificaciones, etc.

Conceptos Fundamentales

Layouts adaptativos: Uso de dp (Density-independent Pixels) para tamaños y sp (Scale-independent Pixels) para fuentes. El tamaño mínimo recomendado para un elemento táctil es 48dp.

Múltiples Puntos de Entrada: Los Intents permiten lanzar componentes de tu app o de otras (ej. abrir un mapa).

Actividades (Activities): Pantallas individuales. Poseen un ciclo de vida (onCreate, onStart, onResume, onPause, onStop, onDestroy).

Single Activity Architecture: Tendencia moderna donde existe una sola Activity y la navegación se hace intercambiando componentes internos (Composables).

2. Fundamentos de Programación en Kotlin

Kotlin es el lenguaje principal para Jetpack Compose. Es fuertemente tipado con inferencia de tipos y Null Safety.

Variables y Colecciones

val: Constantes (solo lectura).

var: Variables (mutables).

// Listas
val readOnlyShapes = listOf("triangle", "square", "circle")
val shapes: MutableList<String> = mutableListOf("triangle", "square")

// Mapas
val readOnlyJuiceMenu = mapOf("apple" to 100, "kiwi" to 190)


Control de Flujo y Ciclos

// When (Switch superpoderoso)
val trafficAction = when (trafficLightState) {
    "Green" -> "Go"
    "Yellow" -> "Slow down"
    "Red" -> "Stop"
    else -> "Malfunction"
}

// Rangos e Iteradores
for (number in 1..5 step 2) { print(number) } // 1, 3, 5


Funciones y Lambdas

// Parámetros con nombre y por defecto
fun printMessageWithPrefix(message: String, prefix: String = "Info") { ... }

// Funciones Lambda
val upperCaseString = { text: String -> text.uppercase() }

// Trailing Lambda Syntax (Si la lambda es el último parámetro, sale de los paréntesis)
val positives = numbers.filter { x -> x > 0 }


Data Classes y Null Safety

Data Classes: Generan automáticamente toString(), equals(), y copy().

data class User(val name: String, val id: Int)


Null Safety y Operador Elvis (?:): Evita NullPointerExceptions.

val nullString: String? = null
println(nullString?.length ?: 0) // Retorna 0 si es nulo


3. Interfaces de Usuario con Jetpack Compose

Jetpack Compose es un framework declarativo basado en Kotlin (adiós a los XML para las vistas).

Conceptos Clave

Recomposición: La UI se actualiza automáticamente cuando cambia su estado. Compose reconstruye inteligentemente solo los nodos afectados.

Modificadores (Modifiers): Decoran los componentes (padding, clickable, fillMaxWidth, size, weight).

Layouts Estándar

Column: Organiza elementos verticalmente.

Row: Organiza elementos horizontalmente.

Box: Superpone elementos (útil para colocar texto sobre imágenes usando matchParentSize()).

@Composable
fun ArtistCardRow(artist: Artist) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(bitmap = artist.image, contentDescription = "Artist")
        Column(modifier = Modifier.weight(1f)) {
            Text(artist.name)
            Text(artist.lastSeenOnline)
        }
    }
}


Componentes Slot-Based (Scaffold)

Permite estructurar pantallas complejas con TopBar, BottomBar, FAB.

Scaffold(
    topBar = { TopAppBar(title = { Text("App") }) },
    bottomBar = { BottomAppBar { ... } }
) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues)) { /* Contenido */ }
}


Listas Eficientes (Lazy Components)

No uses Column con forEach para listas largas, usa LazyColumn (similar a RecyclerView).

LazyColumn {
    items(countries) { item ->
        ElevatedCard(modifier = Modifier.clickable { /* Acción */ }) {
            Text(item.name)
        }
    }
}


Navegación (navigation-compose)

// 1. Definir Rutas
enum class AppScreens { Home, Detail }

// 2. Crear NavHost
@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.Home.name) {
        composable(route = AppScreens.Home.name) { HomeScreen(navController) }
        composable(route = "${AppScreens.Detail.name}/{name}") { backStackEntry -> 
            val name = backStackEntry.arguments?.getString("name")
            DetailScreen(navController, name) 
        }
    }
}


Nota: Para parámetros complejos (objetos), usar serialización (@Serializable) y pasar el objeto en la ruta.

4. Manejo de Estado y Arquitectura

Estado en Compose

El estado es cualquier valor que puede cambiar (el texto de un input, datos de red). Para que Compose reaccione a cambios, usamos remember y mutableStateOf.

var counter by remember { mutableStateOf(0) }
Button(onClick = { counter++ }) { Text("Click me!") }


State Hoisting (Elevación de Estado)

Patrón para hacer componentes Stateless (sin estado), moviendo el estado al invocador o al ViewModel.
Se pasan dos parámetros: value: T y onValueChange: (T) -> Unit.

ViewModels

Separan la lógica de negocio de la UI.

data class SolutionState(val text: String = "")

class SolutionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SolutionState())
    val uiState: StateFlow<SolutionState> = _uiState.asStateFlow()

    fun updateText(newText: String) {
        _uiState.update { it.copy(text = newText) }
    }
}

// En el Composable:
@Composable
fun MyScreen(model: SolutionViewModel = viewModel()) {
    val state by model.uiState.collectAsState()
    TextField(value = state.text, onValueChange = { model.updateText(it) })
}


5. Acceso a Hardware y Permisos

Android requiere declarar permisos en el AndroidManifest.xml.

Permisos Normales: Se otorgan automáticamente (ej. INTERNET).

Permisos Riesgosos (Dangerous): Requieren confirmación del usuario en tiempo de ejecución (ej. CAMERA, READ_CONTACTS, ACCESS_FINE_LOCATION).

Flujo de Permisos con Accompanist (Compose)

Librería recomendada (aunque experimental) para permisos en Compose.

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionAccompanist() {
    val permission = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    
    if (permission.status.isGranted) {
        Text("Permiso Concedido")
        // Ejecutar lógica de contactos (ContentResolver)
    } else {
        Column {
            Text(if (permission.status.shouldShowRationale) "Se requiere para ver contactos." else "Otorgue permiso.")
            Button(onClick = { permission.launchPermissionRequest() }) { Text("Solicitar Permiso") }
        }
    }
}


Activity Result API (Cámara y Galería)

Permite delegar tareas a otras apps y recibir el resultado (ej. tomar una foto) ¡Sin necesidad de permisos de almacenamiento/cámara!

// Galería
var imageUri by remember { mutableStateOf<Uri?>(null) }
val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    imageUri = uri
}
Button(onClick = { gallery.launch("image/*") }) { Text("Abrir Galería") }

// Mostrar imagen con COIL
AsyncImage(model = imageUri, contentDescription = "Imagen")


6. Localización, Sensores y Mapas

Sensores (Ej: Luminosidad)

Requiere usar SensorManager y SensorEventListener. Para no malgastar batería, registrar en un DisposableEffect (se inicia al entrar, se limpia al salir).

DisposableEffect(Unit) {
    sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    onDispose { sensorManager.unregisterListener(sensorListener) }
}


Localización y GPS (Fused Location Provider)

Requiere dependencias de Google Play Services y permisos ACCESS_COARSE_LOCATION y ACCESS_FINE_LOCATION.

Última ubicación conocida: locationClient.lastLocation.addOnSuccessListener {...}

Actualizaciones periódicas (Tracking): Crear un LocationRequest (precisión e intervalos) y un LocationCallback. Usar requestLocationUpdates y removeLocationUpdates en el DisposableEffect.

Distancia: Cálculo de distancia con la fórmula del Haversine a partir de lat/long.

Mapas: Google Maps vs OpenStreetMaps

Google Maps (maps-compose):

Requiere API KEY en el Manifest (idealmente oculta en local.properties y usando secrets-gradle-plugin).

Soporte para Marcadores (personalizados con BitmapDescriptor), Snippets, MapUiSettings.

Geocoder: Convierte coordenadas en direcciones de texto y viceversa.

Estilos personalizados (JSON desde res/raw).

OpenStreetMaps (osmdroid):

Gratis, no requiere API KEY. Configurar userAgentValue.

Requiere usar AndroidView dentro de Compose para incrustar el mapa clásico.

Soporte de rutas usando OSM Bonuspack (OSRMRoadManager).

7. Backend como Servicio (BaaS): Firebase

Firebase es la solución insignia de Google para Backends móviles sin preocuparse de la infraestructura.

Autenticación

Configurar dependencias e inicializar FirebaseAuth.getInstance().

fun login(email: String, pass: String, controller: NavController, context: Context) {
    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
        if (task.isSuccessful) { controller.navigate(AppScreens.home.name) }
        else { Toast.makeText(context, "Error: ${task.exception}", Toast.LENGTH_SHORT).show() }
    }
}


Realtime Database

Base de datos NoSQL documental, basada en JSON, que sincroniza datos en tiempo real. Se recomiendan estructuras planas sin mucho anidamiento.

Escribir:

val database = FirebaseDatabase.getInstance()
val myRef = database.getReference("users/${auth.currentUser!!.uid}")
myRef.setValue(MyUser(name, lastName, age))


Lectura y Suscripción a cambios: Usar addValueEventListener(). Para leer solo una vez usar addListenerForSingleValueEvent(). Es vital remover los listeners (removeEventListener) en el onCleared del ViewModel para evitar fugas de memoria.

Storage

Almacenamiento de archivos binarios (imágenes, PDFs). Se usan StorageReference, putFile() (para subir) y getFile() o descargas asíncronas para leer.

Cloud Messaging (FCM) - Notificaciones Push

Permite enviar notificaciones generadas en la nube hacia el dispositivo.

Esquema de ahorro de batería: Delega la escucha al sistema (Google Play Services) sin requerir servicios background corriendo permanentemente.

Se implementa heredando de FirebaseMessagingService y sobreescribiendo onMessageReceived.

Requiere permiso <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/> (Desde Android 13).

8. Alternativas de Backend: Parse Platform y Docker

Parse Platform es un BaaS Open Source que requiere proveer infraestructura propia (AWS, GCP, servidores locales) o usar servicios como Back4App.

Ofrece BD (MongoDB/PostgreSQL), Auth, LiveQueries (equivalente a Realtime DB de FB) y Push Notifications.

Despliegue con Docker: Herramienta para contenerizar aplicaciones. Se usa docker-compose.yml para orquestar la BD Mongo, Parse Server y Parse Dashboard.

Integración Parse - Android:

// Inicialización (En la clase Application)
Parse.initialize(new Parse.Configuration.Builder(this)
    .applicationId("myappid")
    .server("http://MI_IP:1337/parse/")
    .build()
);

// Escribir datos
ParseObject obj = new ParseObject("SmartUser");
obj.put("name", "Juan");
obj.saveInBackground();

// LiveQuery (Suscripción a cambios en tiempo real)
ParseLiveQueryClient client = ParseLiveQueryClient.Factory.getClient();
SubscriptionHandling<ParseObject> sub = client.subscribe(ParseQuery.getQuery("SmartUser"));
sub.handleEvents((query, event, object) -> { /* Actualizar UI */ });


9. Servicios Adicionales: REST, Notificaciones y Background

Consumo de Servicios REST (Volley)

Librería de Google para peticiones de red. Alternativamente se usa Retrofit/Ktor.

val queue = Volley.newRequestQueue(context)
val req = StringRequest(Request.Method.GET, "URL_HERE",
    { response -> Log.i("Response", response) },
    { error -> Log.e("Error", error.toString()) })
queue.add(req)


Notificaciones Locales

A partir de Android 8.0 (Oreo) es obligatorio crear Notification Channels (Canales de Notificación) asignando niveles de importancia.

// Creación de la notificación (requiere NotificationManager y CompatBuilder)
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle(title)
    .setContentText(message)
    .setSmallIcon(R.drawable.ic_notification)
    .build()


Trabajos en Segundo Plano (Background Services)

Debido a las restricciones de SO en versiones recientes de Android (para cuidar batería), ejecutar código pesado en el background requiere:

Foreground Services: Para tareas que el usuario nota (Ej. reproducir música, GPS de Waze). Muestran una notificación permanente que no se puede borrar ("Sticky").

JobIntentService / WorkManager: Para tareas asíncronas aplazables que el SO organiza eficientemente.

Boot Service: Se usan BroadcastReceivers escuchando la acción BOOT_COMPLETED para levantar un servicio apenas arranca el teléfono (útil para conectarse a Parse LiveQuery en el background).

Fin del documento de curso.
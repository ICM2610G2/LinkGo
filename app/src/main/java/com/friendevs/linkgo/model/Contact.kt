package com.friendevs.linkgo.model

data class Contact(
    val fullName: String,
    val time: String,
    val message: String
)

val Contacts = listOf(
    Contact("David Lopez", "Hace 2 min", "Hola como estas, que tal..."),
    Contact("Nicolas Angulo", "Hace 5 min", "¡Ya estoy aquí! La iluminación está..."),
    Contact("Amigos y Fotos", "Hace 15 min", "Juan: ¿Pudiste revisar los nuevos assets..."),
    Contact("Valentina Gómez", "Hace 1 h", "Encontré un escape room buenísimo..."),
    Contact("Diego Fernando", "Hace 2 h", "Recuerda que el nuevo horario..."),
    Contact("Camila Herrera", "Ayer", "Ya dejé listos los flyers de Pro..."),
    Contact("Javier Silva", "Ayer", "¿Vamos a almorzar mañana? Cono...")
)
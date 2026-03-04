package com.friendevs.linkgo.model

data class MeetUpContact(
    val fullName: String,
    val distance: String,
    val location: String,
    val isSelected: Boolean = false
)

val meetUpContacts = listOf(
    MeetUpContact("David Lopez", "0.8 km de distancia", "Cerca"),
    MeetUpContact("Nicolas Angulo", "4.1 km de distancia", "SoMa"),
    MeetUpContact("Valentina Gómez", "1.2 km de distancia", "Hayes Valley"),
    MeetUpContact("Diego Fernando", "2.5 km de distancia", "Mission District"),
    MeetUpContact("Camila Herrera", "3.3 km de distancia", "Downtown")
)
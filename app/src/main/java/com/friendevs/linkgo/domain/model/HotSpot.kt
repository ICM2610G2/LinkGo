package com.friendevs.linkgo.domain.model

data class Hotspot(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val fotos: Int = 0,
    val url: String = "",
    val address: String? = null,
    val creatorId: String = ""
)
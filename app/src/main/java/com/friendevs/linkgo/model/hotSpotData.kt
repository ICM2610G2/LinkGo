package com.friendevs.linkgo.model

data class Hotspot(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val fotos: Int,
    val url: String,
    val address: String? = null
)
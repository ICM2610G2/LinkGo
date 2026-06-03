package com.friendevs.linkgo.domain.model

data class UserLocation(
    val uid: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val profilePhotoUrl: String = ""
)

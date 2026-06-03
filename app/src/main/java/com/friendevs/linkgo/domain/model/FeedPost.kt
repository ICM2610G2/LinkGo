package com.friendevs.linkgo.domain.model

/**
 * Publicacion del Feed estilo BeReal. Se ancla en el mapa con lat/lng.
 * Constructor sin argumentos requerido por Firebase Realtime Database.
 */
data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val groupId: String = "",
    val timestamp: Long = 0L
)

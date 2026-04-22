package com.friendevs.linkgo.domain.model

import com.google.android.gms.maps.model.LatLng

data class LinkGoRoute(
    val points: List<LatLng>,
    val distanceText: String,
    val durationText: String
)

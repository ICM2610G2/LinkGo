package com.friendevs.linkgo.ui.feature.routes

import com.google.android.gms.maps.model.LatLng

fun decodePolyline(encoded: String): List<LatLng> {
    val points = mutableListOf<LatLng>()
    var index = 0
    var latitude = 0
    var longitude = 0

    while (index < encoded.length) {
        var result = 0
        var shift = 0
        var b: Int

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val deltaLat = if (result and 1 != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }
        latitude += deltaLat

        result = 0
        shift = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val deltaLng = if (result and 1 != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }
        longitude += deltaLng

        points.add(
            LatLng(
                latitude / 1e5,
                longitude / 1e5
            )
        )
    }

    return points
}

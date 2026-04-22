package com.friendevs.linkgo.ui.feature.routes

import com.friendevs.linkgo.domain.model.LinkGoRoute
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private const val GOOGLE_DIRECTIONS_BASE_URL =
    "https://maps.googleapis.com/maps/api/directions/json"

private const val LINKGO_MAPS_API_KEY = "AIzaSyBmmbtudP67euznyKoTbqUXFojfu_HpmSw"

suspend fun fetchLinkGoRoute(origin: LatLng, destination: LatLng): LinkGoRoute {
    return withContext(Dispatchers.IO) {
        val originParam = "${origin.latitude},${origin.longitude}"
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val requestUrl =
            "$GOOGLE_DIRECTIONS_BASE_URL?origin=$originParam&destination=$destinationParam&mode=driving&language=es&key=$LINKGO_MAPS_API_KEY"

        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            doInput = true
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("Directions HTTP error: $responseCode")
            }

            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            parseDirectionsResponse(responseBody)
        } finally {
            connection.disconnect()
        }
    }
}

private fun parseDirectionsResponse(responseBody: String): LinkGoRoute {
    val root = JSONObject(responseBody)
    val status = root.optString("status")
    if (status != "OK") {
        throw IllegalStateException("Directions status: $status")
    }

    val routes = root.optJSONArray("routes")
        ?: throw IllegalStateException("Directions response without routes")
    if (routes.length() == 0) {
        throw IllegalStateException("No routes found")
    }

    val route = routes.getJSONObject(0)
    val polyline = route
        .getJSONObject("overview_polyline")
        .getString("points")

    val legs = route.optJSONArray("legs")
        ?: throw IllegalStateException("Directions response without legs")
    if (legs.length() == 0) {
        throw IllegalStateException("No leg information found")
    }

    val leg = legs.getJSONObject(0)
    val distanceText = leg.getJSONObject("distance").optString("text")
    val durationText = leg.getJSONObject("duration").optString("text")

    return LinkGoRoute(
        points = decodePolyline(polyline),
        distanceText = distanceText,
        durationText = durationText
    )
}

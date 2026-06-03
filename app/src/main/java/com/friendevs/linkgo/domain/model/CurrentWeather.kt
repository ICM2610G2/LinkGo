package com.friendevs.linkgo.domain.model

data class CurrentWeather(
    val temperatureCelsius: Double,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val icon: String,
    val description: String
)

package com.friendevs.linkgo.data.repository

import com.friendevs.linkgo.domain.model.CurrentWeather
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.abs

class WeatherRepository {

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): CurrentWeather {
        val cached = cachedWeather
        val now = System.currentTimeMillis()
        if (cached != null &&
            now - cached.fetchedAtMillis < CACHE_DURATION_MILLIS &&
            isNear(cached.latitude, cached.longitude, latitude, longitude)
        ) {
            return cached.weather
        }

        val response = api.getForecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code,wind_speed_10m"
        )
        val current = response.current
            ?: throw IllegalStateException("Weather response without current data")

        val weather = CurrentWeather(
            temperatureCelsius = current.temperature,
            weatherCode = current.weatherCode,
            windSpeedKmh = current.windSpeed,
            icon = weatherIconFor(current.weatherCode),
            description = weatherDescriptionFor(current.weatherCode)
        )

        cachedWeather = CachedWeather(
            latitude = latitude,
            longitude = longitude,
            fetchedAtMillis = now,
            weather = weather
        )
        return weather
    }

    private fun isNear(
        cachedLatitude: Double,
        cachedLongitude: Double,
        latitude: Double,
        longitude: Double
    ): Boolean {
        return abs(cachedLatitude - latitude) < COORDINATE_CACHE_DELTA &&
            abs(cachedLongitude - longitude) < COORDINATE_CACHE_DELTA
    }

    private fun weatherIconFor(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        95 -> "⛈️"
        else -> "🌡️"
    }

    private fun weatherDescriptionFor(code: Int): String = when (code) {
        0 -> "Soleado"
        1, 2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nieve"
        95 -> "Tormenta"
        else -> "Clima actual"
    }

    private interface OpenMeteoApi {
        @GET("v1/forecast")
        suspend fun getForecast(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("current") current: String
        ): OpenMeteoResponse
    }

    private data class OpenMeteoResponse(
        val current: OpenMeteoCurrent?
    )

    private data class OpenMeteoCurrent(
        @SerializedName("temperature_2m")
        val temperature: Double,
        @SerializedName("weather_code")
        val weatherCode: Int,
        @SerializedName("wind_speed_10m")
        val windSpeed: Double
    )

    private data class CachedWeather(
        val latitude: Double,
        val longitude: Double,
        val fetchedAtMillis: Long,
        val weather: CurrentWeather
    )

    companion object {
        private const val CACHE_DURATION_MILLIS = 15 * 60 * 1000L
        private const val COORDINATE_CACHE_DELTA = 0.05

        private val api: OpenMeteoApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApi::class.java)
        }

        @Volatile
        private var cachedWeather: CachedWeather? = null
    }
}

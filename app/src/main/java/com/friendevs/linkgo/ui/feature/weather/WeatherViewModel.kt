package com.friendevs.linkgo.ui.feature.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.WeatherRepository
import com.friendevs.linkgo.domain.model.CurrentWeather
import kotlinx.coroutines.launch
import kotlin.math.abs

data class WeatherState(
    val isLoading: Boolean = false,
    val weather: CurrentWeather? = null,
    val error: String? = null
)

class WeatherViewModel : ViewModel() {
    var state by mutableStateOf(WeatherState())
        private set

    private val repository = WeatherRepository()
    private var lastRequestAtMillis = 0L
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    fun loadWeather(latitude: Double, longitude: Double) {
        val now = System.currentTimeMillis()
        if (state.isLoading) return
        if (state.weather != null &&
            now - lastRequestAtMillis < REQUEST_THROTTLE_MILLIS &&
            isNearLastRequest(latitude, longitude)
        ) {
            return
        }

        lastRequestAtMillis = now
        lastLatitude = latitude
        lastLongitude = longitude

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            runCatching {
                repository.getCurrentWeather(latitude, longitude)
            }.onSuccess { weather ->
                state = WeatherState(weather = weather)
            }.onFailure { error ->
                state = WeatherState(
                    weather = state.weather,
                    error = error.message ?: "No se pudo cargar el clima"
                )
            }
        }
    }

    private fun isNearLastRequest(latitude: Double, longitude: Double): Boolean {
        val previousLatitude = lastLatitude ?: return false
        val previousLongitude = lastLongitude ?: return false
        return abs(previousLatitude - latitude) < COORDINATE_REQUEST_DELTA &&
            abs(previousLongitude - longitude) < COORDINATE_REQUEST_DELTA
    }

    companion object {
        private const val REQUEST_THROTTLE_MILLIS = 15 * 60 * 1000L
        private const val COORDINATE_REQUEST_DELTA = 0.05
    }
}

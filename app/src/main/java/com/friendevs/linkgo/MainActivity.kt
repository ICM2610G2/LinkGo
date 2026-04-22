package com.friendevs.linkgo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.fragment.app.FragmentActivity // Tu import para la huella
import androidx.lifecycle.viewmodel.compose.viewModel
import com.friendevs.linkgo.model.SensorViewModel // Tus sensores
import com.friendevs.linkgo.ui.navigation.Navigation // La ruta nueva de tus compañeros
import com.friendevs.linkgo.ui.theme.LinkGoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.libraries.places.api.Places

lateinit var auth: FirebaseAuth
lateinit var database: FirebaseDatabase

@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() { // Mantenemos tu FragmentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        enableEdgeToEdge()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBmmbtudP67euznyKoTbqUXFojfu_HpmSw")
        }

        setContent {
            // Inyectamos el ViewModel de los sensores
            val sensorViewModel: SensorViewModel = viewModel()

            // Usamos el tema con el sensor de luz
            LinkGoTheme(
                sensorViewModel = sensorViewModel,
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                // Pasamos el sensorViewModel a la navegación (importante para el acelerómetro global)
                Navigation(sensorViewModel = sensorViewModel)
            }
        }
    }
}
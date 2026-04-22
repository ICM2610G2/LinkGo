package com.friendevs.linkgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import com.friendevs.linkgo.model.SensorViewModel
import com.friendevs.linkgo.navigation.Navigation
import com.friendevs.linkgo.ui.theme.LinkGoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.libraries.places.api.Places

lateinit var auth: FirebaseAuth
lateinit var database: FirebaseDatabase

@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        enableEdgeToEdge()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBmmbtudP67euznyKoTbqUXFojfu_HpmSw")
        }

        setContent {
            val sensorViewModel: SensorViewModel = viewModel()

            LinkGoTheme(sensorViewModel = sensorViewModel, darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
                Navigation(sensorViewModel)
            }
        }
    }
}
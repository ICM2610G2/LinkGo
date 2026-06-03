package com.friendevs.linkgo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity // Tu import para la huella
import androidx.lifecycle.viewmodel.compose.viewModel
import com.friendevs.linkgo.model.SensorViewModel // Tus sensores
import com.friendevs.linkgo.service.FcmTokenManager
import com.friendevs.linkgo.service.LinkGoMessagingService
import com.friendevs.linkgo.ui.navigation.Navigation // La ruta nueva de tus compañeros
import com.friendevs.linkgo.ui.theme.LinkGoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.libraries.places.api.Places

lateinit var auth: FirebaseAuth
lateinit var database: FirebaseDatabase

@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() { // Mantenemos tu FragmentActivity

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    /** groupId del chat a abrir por deep-link de notificacion. Consumido por Navigation. */
    private var pendingChatGroupId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        enableEdgeToEdge()

        pendingChatGroupId = intent?.getStringExtra(LinkGoMessagingService.EXTRA_GROUP_ID)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBmmbtudP67euznyKoTbqUXFojfu_HpmSw")
        }

        LinkGoMessagingService.createChannel(this)
        requestNotificationPermission()
        FcmTokenManager.registerCurrentToken()

        setContent {
            val sensorViewModel: SensorViewModel = viewModel()


            LinkGoTheme(
                sensorViewModel = sensorViewModel,
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {

                Navigation(
                    sensorViewModel = sensorViewModel,
                    deepLinkGroupId = pendingChatGroupId,
                    onDeepLinkConsumed = { pendingChatGroupId = null }
                )
            }
        }
    }

    /** App ya viva: nueva notificacion tocada llega aqui (FLAG_ACTIVITY_SINGLE_TOP). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(LinkGoMessagingService.EXTRA_GROUP_ID)?.let {
            pendingChatGroupId = it
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}

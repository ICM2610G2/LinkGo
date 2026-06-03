package com.friendevs.linkgo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.friendevs.linkgo.MainActivity
import com.friendevs.linkgo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LinkGoMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "linkgo_default"
        const val CHANNEL_NAME = "Notificaciones LinkGo"

        /** Extra usado para deep-link: groupId del chat a abrir al tocar la notificacion. */
        const val EXTRA_GROUP_ID = "extra_group_id"

        /** Crea el canal de notificaciones (Android 8+). Idempotente. */
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FcmTokenManager.saveToken(uid, token)
    }

    // MainActivity esta anotada @ExperimentalMaterial3Api; referenciarla aqui requiere opt-in.
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "LinkGo"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val groupId = message.data["groupId"]

        createChannel(this)

        // Deep-link: al tocar la notificacion abre MainActivity, que enruta
        // a ChatDetail/{groupId} leyendo este extra.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (groupId != null) putExtra(EXTRA_GROUP_ID, groupId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            groupId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

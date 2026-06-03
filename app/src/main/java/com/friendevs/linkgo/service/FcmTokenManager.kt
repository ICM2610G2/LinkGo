package com.friendevs.linkgo.service

import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "No se pudo obtener el token FCM", task.exception)
                return@addOnCompleteListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val token = task.result
            if (uid.isNullOrBlank() || token.isNullOrBlank()) return@addOnCompleteListener

            saveToken(uid, token)
        }
    }

    fun saveToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return

        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        val tokenData = mapOf(
            "token" to token,
            "updatedAt" to System.currentTimeMillis()
        )
        val updates = mapOf<String, Any>(
            "fcmToken" to token,
            "fcmTokens/${tokenKey(token)}" to tokenData
        )

        userRef.updateChildren(updates)
            .addOnFailureListener { Log.w(TAG, "No se pudo guardar el token FCM", it) }
    }

    fun clearCurrentToken(onComplete: () -> Unit = {}) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            onComplete()
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result
            if (!task.isSuccessful || token.isNullOrBlank()) {
                onComplete()
                return@addOnCompleteListener
            }

            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            userRef.child("fcmTokens").child(tokenKey(token)).removeValue()
                .addOnCompleteListener {
                    userRef.child("fcmToken").get()
                        .addOnCompleteListener { legacyTask ->
                            val legacyToken = legacyTask.result?.getValue(String::class.java)
                            if (legacyToken == token) {
                                userRef.child("fcmToken").removeValue()
                                    .addOnCompleteListener { onComplete() }
                            } else {
                                onComplete()
                            }
                        }
                }
        }
    }

    fun clearCurrentTokenAndDeleteInstallationToken(onComplete: () -> Unit = {}) {
        clearCurrentToken {
            FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener { onComplete() }
        }
    }

    private fun tokenKey(token: String): String =
        Base64.encodeToString(
            token.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
}

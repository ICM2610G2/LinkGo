package com.friendevs.linkgo.data.repository

import android.net.Uri
import android.util.Log
import com.friendevs.linkgo.domain.model.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FeedRepository {

    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    /**
     * Sube la foto a Storage, lee la ubicacion actual del usuario y crea el post en /feed.
     */
    suspend fun createPost(fileUri: Uri, caption: String, groupId: String): FeedPost {
        val uid = auth.currentUser?.uid ?: error("Usuario no autenticado")
        val ts = System.currentTimeMillis()
        val fileName = "feed_$ts.jpg"
        val photoRef = storage.child("feed/$uid/$fileName")

        photoRef.putFile(fileUri).await()
        val imageUrl = photoRef.downloadUrl.await().toString()

        // Ubicacion actual desde /locations/{uid} (la publica el mapa).
        val locSnap = db.child("locations").child(uid).get().await()
        val lat = (locSnap.child("lat").value as? Number)?.toDouble() ?: 0.0
        val lng = (locSnap.child("lng").value as? Number)?.toDouble() ?: 0.0

        // Datos del autor.
        val userSnap = db.child("users").child(uid).get().await()
        val authorName = userSnap.child("name").getValue(String::class.java)
            ?: auth.currentUser?.displayName ?: "Usuario"
        val authorPhotoUrl = userSnap.child("profilePhotoUrl").getValue(String::class.java).orEmpty()

        val postRef = db.child("feed").push()
        val post = FeedPost(
            id = postRef.key ?: ts.toString(),
            authorId = uid,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            imageUrl = imageUrl,
            caption = caption,
            lat = lat,
            lng = lng,
            groupId = groupId,
            timestamp = ts
        )
        postRef.setValue(post).await()
        return post
    }

    /** Escucha en tiempo real todos los posts del feed (mas recientes primero). */
    fun observeFeed(onResult: (List<FeedPost>) -> Unit): ValueEventListener {
        val ref = db.child("feed")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { it.getValue(FeedPost::class.java) }
                onResult(posts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedRepository", "observeFeed cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeFeedListener(listener: ValueEventListener) {
        db.child("feed").removeEventListener(listener)
    }

    /** True si el usuario ya publico una foto hoy (gating estilo BeReal). */
    suspend fun hasPostedToday(uid: String): Boolean {
        val snapshot = db.child("feed").get().await()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return snapshot.children.any { child ->
            val post = child.getValue(FeedPost::class.java)
            post != null && post.authorId == uid && post.timestamp >= startOfDay
        }
    }

    fun toggleLike(postId: String) {
        val uid = auth.currentUser?.uid ?: return
        val postRef = db.child("feed").child(postId)
        postRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val post = currentData.getValue(FeedPost::class.java)
                    ?: return com.google.firebase.database.Transaction.success(currentData)

                val likedBy = post.likedBy.toMutableMap()
                if (likedBy.containsKey(uid)) {
                    likedBy.remove(uid)
                } else {
                    likedBy[uid] = true
                }

                currentData.value = post.copy(
                    likedBy = likedBy,
                    likesCount = likedBy.size
                )
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("FeedRepository", "toggleLike failed", error.toException())
                }
            }
        })
    }
}

package com.friendevs.linkgo.ui.feature.map

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HotspotGalleryState(
    val photoUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

class HotspotGalleryViewModel : ViewModel() {

    var state by mutableStateOf(HotspotGalleryState())
        private set

    private val storage = FirebaseStorage.getInstance().reference
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun loadPhotos(hotspotId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val items = storage
                    .child("hotspots/$hotspotId")
                    .listAll()
                    .await()
                    .items
                    .sortedByDescending { it.name }

                val urls = items.mapNotNull {
                    runCatching { it.downloadUrl.await().toString() }.getOrNull()
                }
                state = state.copy(photoUrls = urls, isLoading = false)
            } catch (e: Exception) {
                Log.e("HotspotGalleryVM", "Error loading photos: ${e.message}")
                state = state.copy(isLoading = false, error = "No se pudieron cargar las fotos")
            }
        }
    }

    fun uploadPhoto(hotspotId: String, fileUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val fileName = "${System.currentTimeMillis()}_$uid.jpg"
        val photoRef = storage.child("hotspots/$hotspotId/$fileName")

        viewModelScope.launch {
            state = state.copy(isUploading = true, error = null)
            try {
                photoRef.putFile(fileUri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()
                state = state.copy(
                    photoUrls = listOf(downloadUrl) + state.photoUrls,
                    isUploading = false
                )
                val snap = db.child("hotspots/$hotspotId/fotos").get().await()
                val current = (snap.value as? Long)?.toInt() ?: 0
                db.child("hotspots/$hotspotId/fotos").setValue(current + 1)
            } catch (e: Exception) {
                Log.e("HotspotGalleryVM", "Error uploading: ${e.message}")
                state = state.copy(isUploading = false, error = "Error al subir la foto")
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}

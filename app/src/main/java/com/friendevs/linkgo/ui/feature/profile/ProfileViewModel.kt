package com.friendevs.linkgo.ui.feature.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.LocationRepository
import com.friendevs.linkgo.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val locationRepository = LocationRepository()

    private val _userState =
        MutableStateFlow(User(name = "", username = "@", age = ""))
    val userState = _userState.asStateFlow()

    private val _momentPhotos = MutableStateFlow<List<String>>(emptyList())
    val momentPhotos = _momentPhotos.asStateFlow()

    private val _isUploadingMoment = MutableStateFlow(false)
    val isUploadingMoment = _isUploadingMoment.asStateFlow()

    private val _profilePhotoUrl = MutableStateFlow("")
    val profilePhotoUrl = _profilePhotoUrl.asStateFlow()

    private val _isUploadingProfilePhoto = MutableStateFlow(false)
    val isUploadingProfilePhoto = _isUploadingProfilePhoto.asStateFlow()

    init {
        loadUser()
        loadMomentPhotos()
        loadProfilePhoto()
    }

    private fun loadUser() {
        val userId = auth.currentUser?.uid ?: return
        val myRef = database.getReference("users/$userId")
        myRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                _userState.value = user
            }
        }.addOnFailureListener {
            Log.e("ProfileViewModel", "Error loading user data: ${it.message}")
        }
    }

    fun updateName(newValue: String) {
        _userState.update { it.copy(name = newValue) }
    }

    fun updateLastName(newValue: String) {
        _userState.update { it.copy(lastName = newValue) }
    }

    fun updateUsername(newValue: String) {
        _userState.update { it.copy(username = newValue) }
    }

    fun updateAge(newValue: String) {
        _userState.update { it.copy(age = newValue) }
    }

    fun updateEmail(newValue: String) {
        _userState.update { it.copy(email = newValue) }
    }

    fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val myRef = database.getReference("users/$userId")
        val profileData = mapOf(
            "name" to _userState.value.name,
            "lastName" to _userState.value.lastName,
            "username" to _userState.value.username,
            "age" to _userState.value.age,
            "email" to _userState.value.email,
            "friendsCount" to _userState.value.friendsCount,
            "postsCount" to _userState.value.postsCount,
            "circlesCount" to _userState.value.circlesCount,
            "fcmToken" to _userState.value.fcmToken,
            "profilePhotoUrl" to (_profilePhotoUrl.value.ifBlank { _userState.value.profilePhotoUrl })
        )
        myRef.updateChildren(profileData)
        locationRepository.updateLocationProfile(
            uid = userId,
            name = _userState.value.name.ifBlank { auth.currentUser?.displayName ?: "Usuario" },
            profilePhotoUrl = _profilePhotoUrl.value.ifBlank { _userState.value.profilePhotoUrl }
        )
    }

    fun loadMomentPhotos() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val files = storage
                    .child("Post/$userId")
                    .listAll()
                    .await()
                    .items
                    .sortedByDescending { it.name }

                val urls = files.mapNotNull { reference ->
                    runCatching { reference.downloadUrl.await().toString() }.getOrNull()
                }

                _momentPhotos.value = urls
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading photos: ${e.message}")
            }
        }
    }

    fun uploadMomentPhoto(fileUri: Uri, isFromCamera: Boolean = true) {
        val userId = auth.currentUser?.uid ?: return
        val fileName = "moment_${System.currentTimeMillis()}.jpg"
        val photoRef = storage.child("Post/$userId/$fileName")

        viewModelScope.launch {
            _isUploadingMoment.value = true
            try {
                photoRef.putFile(fileUri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()
                _momentPhotos.update { listOf(downloadUrl) + it }
                incrementPostsCount(userId)
                
                // Solo auto-guardar en hotspots si la foto es de cámara
                if (isFromCamera) {
                    autoSaveToNearbyHotspots(fileUri, fileName)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading photo: ${e.message}")
            } finally {
                _isUploadingMoment.value = false
            }
        }
    }

    private fun autoSaveToNearbyHotspots(fileUri: Uri, fileName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Obtener ubicación actual del usuario desde Firebase
                val locSnap = database.getReference("locations/$userId").get().await()
                val userLat = (locSnap.child("lat").value as? Double) ?: return@launch
                val userLng = (locSnap.child("lng").value as? Double) ?: return@launch

                // 2. Obtener todos los grupos a los que pertenece el usuario
                val groupsSnap = database.getReference("groups").get().await()
                val myGroupIds = mutableListOf<String>()
                groupsSnap.children.forEach { groupSnap ->
                    val isMember = groupSnap.child("members").child(userId)
                        .getValue(Boolean::class.java) ?: false
                    if (isMember) {
                        val gid = groupSnap.key ?: return@forEach
                        myGroupIds.add(gid)
                    }
                }
                if (myGroupIds.isEmpty()) return@launch

                // 3. Buscar hotspots de esos grupos que estén cerca del usuario
                val hotspotsSnap = database.getReference("hotspots").get().await()
                val nearbyHotspotIds = mutableListOf<String>()
                hotspotsSnap.children.forEach { hotSpotSnap ->
                    val groupId = hotSpotSnap.child("groupId").value as? String ?: return@forEach
                    if (groupId !in myGroupIds) return@forEach
                    val hotLat = (hotSpotSnap.child("lat").value as? Double) ?: return@forEach
                    val hotLng = (hotSpotSnap.child("lng").value as? Double) ?: return@forEach
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(userLat, userLng, hotLat, hotLng, results)
                    if (results[0] <= HOTSPOT_RADIUS_METERS) {
                        nearbyHotspotIds.add(hotSpotSnap.key ?: return@forEach)
                    }
                }

                // 4. Subir la foto a cada hotspot cercano e incrementar contador
                nearbyHotspotIds.forEach { hotspotId ->
                    try {
                        storage.child("hotspots/$hotspotId/$fileName").putFile(fileUri).await()
                        val snap = database.getReference("hotspots/$hotspotId/fotos").get().await()
                        val current = (snap.value as? Long)?.toInt() ?: 0
                        database.getReference("hotspots/$hotspotId/fotos").setValue(current + 1)
                        Log.d("ProfileViewModel", "Momento guardado automáticamente en hotspot $hotspotId")
                    } catch (e: Exception) {
                        Log.w("ProfileViewModel", "No se pudo guardar en hotspot $hotspotId: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "Auto-guardado en hotspots omitido: ${e.message}")
            }
        }
    }

    fun loadProfilePhoto() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val latestPhoto = storage
                    .child("ptps/$userId")
                    .listAll()
                    .await()
                    .items
                    .sortedByDescending { it.name }
                    .firstOrNull()

                val downloadUrl = latestPhoto?.downloadUrl?.await()?.toString().orEmpty()
                _profilePhotoUrl.value = downloadUrl
                _userState.update { it.copy(profilePhotoUrl = downloadUrl) }
                locationRepository.updateLocationProfile(
                    uid = userId,
                    name = _userState.value.name.ifBlank { auth.currentUser?.displayName ?: "Usuario" },
                    profilePhotoUrl = downloadUrl
                )
            } catch (e: Exception) {
                _profilePhotoUrl.value = ""
                Log.e("ProfileViewModel", "Error loading profile photo: ${e.message}")
            }
        }
    }

    fun uploadProfilePhoto(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val photoRef = storage.child("ptps/$userId/$fileName")

        viewModelScope.launch {
            _isUploadingProfilePhoto.value = true
            try {
                photoRef.putFile(fileUri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()
                _profilePhotoUrl.value = downloadUrl
                _userState.update { it.copy(profilePhotoUrl = downloadUrl) }
                database.getReference("users/$userId/profilePhotoUrl").setValue(downloadUrl)
                locationRepository.updateLocationProfile(
                    uid = userId,
                    name = _userState.value.name.ifBlank { auth.currentUser?.displayName ?: "Usuario" },
                    profilePhotoUrl = downloadUrl
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading profile photo: ${e.message}")
            } finally {
                _isUploadingProfilePhoto.value = false
            }
        }
    }

    private fun incrementPostsCount(userId: String) {
        val currentCount = _userState.value.postsCount.toIntOrNull() ?: 0
        val updatedCount = (currentCount + 1).toString()

        _userState.update { it.copy(postsCount = updatedCount) }
        database.getReference("users/$userId/postsCount").setValue(updatedCount)
            .addOnFailureListener {
                Log.e("ProfileViewModel", "Error updating posts count: ${it.message}")
            }
    }

    companion object {
        private const val HOTSPOT_RADIUS_METERS = 300f
    }
}

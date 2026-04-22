package com.friendevs.linkgo.ui.feature.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        myRef.setValue(_userState.value)
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

    fun uploadMomentPhoto(fileUri: Uri) {
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
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading photo: ${e.message}")
            } finally {
                _isUploadingMoment.value = false
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

                _profilePhotoUrl.value = latestPhoto?.downloadUrl?.await()?.toString().orEmpty()
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
                _profilePhotoUrl.value = photoRef.downloadUrl.await().toString()
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
}

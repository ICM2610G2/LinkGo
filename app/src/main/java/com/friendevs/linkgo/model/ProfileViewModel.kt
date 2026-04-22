package com.friendevs.linkgo.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userState =
        MutableStateFlow(MyUser(name = "Nicolas", username = "@ndgc", age = "20"))
    val userState = _userState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val userId = auth.currentUser?.uid ?: return
        val myRef = database.getReference("users/$userId")
        myRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(MyUser::class.java)
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
}
package com.friendevs.linkgo.ui.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegisterState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val nombreError: String = "",
    val apellidoError: String = "",
    val emailError: String = "",
    val passwordError: String = ""
)

class RegisterViewModel : ViewModel() {
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState = _registerState.asStateFlow()

    fun updateName(newValue: String) {
        _registerState.update { it.copy(nombre = newValue) } }
    fun updateLastName(newValue: String) {
        _registerState.update { it.copy(apellido = newValue) } }
    fun updateEmail(newValue: String) {
        _registerState.update { it.copy(email = newValue) } }
    fun updatePassword(newValue: String) {
        _registerState.update { it.copy(password = newValue) } }

    fun updateNameError(newValue: String) {
        _registerState.update { it.copy(nombreError = newValue) } }
    fun updateLastError(newValue: String) {
        _registerState.update { it.copy(apellidoError = newValue) } }
    fun updateEmailError(newValue: String) {
        _registerState.update { it.copy(emailError = newValue) } }
    fun updatePassError(newValue: String) {
        _registerState.update { it.copy(passwordError = newValue) } }
}

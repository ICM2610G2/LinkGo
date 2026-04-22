package com.friendevs.linkgo.ui.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val email : String = "",
    val password: String = "",
    val emailError : String = "",
    val passwordError : String = ""
)

class LoginViewModel: ViewModel(){
    private  val _loginState = MutableStateFlow<LoginState>(LoginState())
    val loginState = _loginState.asStateFlow()

    fun updateEmail(newValue: String){
        _loginState.update { it.copy(email = newValue) }
    }
    fun updatePassword(newValue: String){
        _loginState.update { it.copy(password = newValue) }
    }
    fun updateEmailError(newValue: String){
        _loginState.update { it.copy(emailError = newValue) }
    }
    fun updatePassError(newValue: String){
        _loginState.update { it.copy(passwordError = newValue) }
    }
}

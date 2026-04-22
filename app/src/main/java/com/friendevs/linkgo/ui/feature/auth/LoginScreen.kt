package com.friendevs.linkgo.ui.feature.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.auth
import com.friendevs.linkgo.ui.navigation.Screens
import com.friendevs.linkgo.util.validEmailAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


@Composable
fun LoginScreen(navController: NavHostController, model: LoginViewModel) {

    val user by model.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            navController.navigate(Screens.Map.name) {
                popUpTo(Screens.login.name) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "LinkGo logo",
            modifier = Modifier.size(200.dp)
        )

        TextField(
            value = user.email,
            onValueChange = { model.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = user.emailError.isNotEmpty(),
            supportingText = { if (user.emailError.isNotEmpty()) Text(user.emailError) }
        )

        TextField(
            value = user.password,
            onValueChange = { model.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = user.passwordError.isNotEmpty(),
            supportingText = { if (user.passwordError.isNotEmpty()) Text(user.passwordError) }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (validateForm(model, user.email, user.password)){
                    auth.signInWithEmailAndPassword(user.email, user.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate(Screens.Map.name) {
                                    popUpTo(Screens.login.name) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Login error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        ) {
            Text("Login")
        }

        Button(
            onClick = {
                navController.navigate(Screens.register.name)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}
fun validateForm(model: LoginViewModel, email: String, password: String): Boolean {
    if (email.isEmpty()) {
        model.updateEmailError("Email is empty")
        return false
    } else { model.updateEmailError("") }

    if (!validEmailAddress(email)) {
        model.updateEmailError("Not a valid address")
        return false
    } else { model.updateEmailError("") }

    if (password.isEmpty()) {
        model.updatePassError("Password is empty")
        return false
    } else {  model.updatePassError("") }

    if (password.length < 6) {
        model.updatePassError("Password is too short")
        return false
    } else { model.updatePassError("")  }

    return true
}

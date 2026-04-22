package com.friendevs.linkgo.ui.feature.auth

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.auth
import com.friendevs.linkgo.ui.navigation.Screens
import com.friendevs.linkgo.util.validEmailAddress
import com.friendevs.linkgo.ui.feature.auth.LoginViewModel

@Composable
fun LoginScreen(navController: NavHostController, model: LoginViewModel) {

    val user by model.loginState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

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
                                val prefs = context.getSharedPreferences("auth", 0)
                                prefs.edit().putString("email", user.email).
                                putString("password", user.password).apply()
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

        IconButton(
            onClick = {
                activity?.let {
                    showBiometricPrompt(it) {

                        val prefs = context.getSharedPreferences("auth", 0)
                        val savedEmail = prefs.getString("email", null)
                        val password = prefs.getString("password", null)

                        if (savedEmail != null && password != null) {
                            auth.signInWithEmailAndPassword(savedEmail, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate(Screens.Map.name) {
                                            popUpTo(Screens.login.name) { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Biometric login failed", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        } else {
                            Toast.makeText(context, "No session found", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            },
            modifier = Modifier.padding(vertical = 8.dp).size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Huella",
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
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

fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, "Error: $errString", Toast.LENGTH_SHORT).show()
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso Biométrico")
        .setSubtitle("Inicia sesión con tu huella dactilar")
        .setNegativeButtonText("Cancelar")
        .build()
    biometricPrompt.authenticate(promptInfo)
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
    } else { model.updatePassError("") }

    if (password.length < 6) {
        model.updatePassError("Password is too short")
        return false
    } else { model.updatePassError("") }

    return true
}
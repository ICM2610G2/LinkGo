package com.friendevs.linkgo.ui.feature.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.friendevs.linkgo.auth
import com.friendevs.linkgo.domain.model.User
import com.friendevs.linkgo.ui.navigation.Screens
import com.friendevs.linkgo.util.validEmailAddress
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


@Composable
fun RegisterScreen(navController: NavHostController, model: RegisterViewModel) {
    val state by model.registerState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = state.nombre,
            onValueChange = { model.updateName(it) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.nombreError.isNotEmpty(),
            supportingText = { if (state.nombreError.isNotEmpty()) Text(state.nombreError) }
        )

        TextField(
            value = state.apellido,
            onValueChange = { model.updateLastName(it) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.apellidoError.isNotEmpty(),
            supportingText = { if (state.apellidoError.isNotEmpty()) Text(state.apellidoError) }
        )

        TextField(
            value = state.email,
            onValueChange = { model.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.emailError.isNotEmpty(),
            supportingText = { if (state.emailError.isNotEmpty()) Text(state.emailError) }
        )

        TextField(
            value = state.password,
            onValueChange = { model.updatePassword(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError.isNotEmpty(),
            supportingText = { if (state.passwordError.isNotEmpty()) Text(state.passwordError) }
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            onClick = {
                if (validateRegisterForm(model, state)) {
                    auth.createUserWithEmailAndPassword(state.email, state.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val database = FirebaseDatabase.getInstance()
                                val myRef = database.getReference("users/${user?.uid}")
                                val myUser = User(
                                    name = state.nombre,
                                    lastName = state.apellido,
                                    email = state.email,
                                    username = "",
                                    age = "",
                                    friendsCount = "0",
                                    postsCount = "0",
                                    circlesCount = "0"
                                )
                                myRef.setValue(myUser).addOnCompleteListener { dbTask ->
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName("${state.nombre} ${state.apellido}")
                                        .build()
                                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            navController.navigate(Screens.Map.name) {
                                                popUpTo(Screens.login.name) { inclusive = true }        
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        ) {
            Text("Register")
        }
    }
}

fun validateRegisterForm(model: RegisterViewModel, state: RegisterState): Boolean {
    var isValid = true
    if (state.nombre.isEmpty()) {
        model.updateNameError("Name is empty"); isValid = false } else { model.updateNameError("") }
    if (state.apellido.isEmpty()) {
        model.updateLastError("Last name is empty"); isValid = false } else { model.updateLastError("") }
    if (!validEmailAddress(state.email)) {
        model.updateEmailError("Not a valid address"); isValid = false } else { model.updateEmailError("") }
    if (state.password.length < 6) {
        model.updatePassError("Password must be 6+ chars"); isValid = false } else { model.updatePassError("") }
    return isValid
}
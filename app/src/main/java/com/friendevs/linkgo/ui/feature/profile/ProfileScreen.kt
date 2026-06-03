package com.friendevs.linkgo.ui.feature.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.friendevs.linkgo.domain.model.User
import com.friendevs.linkgo.R
import com.friendevs.linkgo.ui.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

@Composable
@ExperimentalMaterial3Api
fun ProfileScreen(navController: NavHostController, model: ProfileViewModel = viewModel()) {
    val user by model.userState.collectAsState()
    val profilePhotoUrl by model.profilePhotoUrl.collectAsState()
    val isUploadingProfilePhoto by model.isUploadingProfilePhoto.collectAsState()
    var showEditBox by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                UserTopAppBar(
                    navController = navController,
                    onSettingsClick = { showEditBox = true }
                )
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                item {
                    ProfileHeader(
                        fullName = "${user.name} ${user.lastName}".trim(),
                        username = user.username,
                        email = user.email,
                        profilePhotoUrl = profilePhotoUrl,
                        isUploadingProfilePhoto = isUploadingProfilePhoto,
                        onProfilePhotoSelected = model::uploadProfilePhoto
                    )
                }
                item { ProfileEditRow(onEditClick = { showEditBox = true }) }
                item { StatsRow(user) }
                item { MomentsGrid(model = model) }
            }
        }

        if (showEditBox) {
            EditProfileBox(
                user = user,
                model = model,
                onDismiss = { showEditBox = false }
            )
        }
    }
}

@Composable
fun EditProfileBox(user: User, model: ProfileViewModel, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Editar Perfil", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = user.name,
                    onValueChange = { model.updateName(it) },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = user.lastName,
                    onValueChange = { model.updateLastName(it) },
                    label = { Text("Apellido") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = user.username,
                    onValueChange = { model.updateUsername(it) },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = user.age,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            model.updateAge(it)
                        }
                    },
                    label = { Text("Edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            model.saveProfile()
                            onDismiss()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditRow(onEditClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onEditClick,
            Modifier
                .width(270.dp)
                .height(50.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Editar Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Button(
            onClick = {},
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartir",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ProfileHeader(
    fullName: String,
    username: String,
    email: String,
    profilePhotoUrl: String,
    isUploadingProfilePhoto: Boolean,
    onProfilePhotoSelected: (Uri) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatar(
            profilePhotoUrl = profilePhotoUrl,
            isUploadingProfilePhoto = isUploadingProfilePhoto,
            onProfilePhotoSelected = onProfilePhotoSelected
        )
        Text(
            fullName, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, fontSize = 30.sp
        )

        Text(
            username,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary, fontSize = 18.sp
        )
        
        if (email.isNotEmpty()) {
            Text(
                email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray, fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StatsRow(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(user.postsCount, "Fotos")
        StatItem(user.friendsCount, "Amigos")
        StatItem(user.circlesCount, "Circulos")
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                fontSize = 25.sp
            )
            Text(label, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun UserTopAppBar(navController: NavHostController, onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Mensajes",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Menu",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screens.login.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ProfileAvatar(
    profilePhotoUrl: String,
    isUploadingProfilePhoto: Boolean,
    onProfilePhotoSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    val cameraPermission = Manifest.permission.CAMERA

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapToCacheUri(context, bitmap, "profile")?.let(onProfilePhotoSelected)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onProfilePhotoSelected(uri)
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) cameraLauncher.launch(null)
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.size(140.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.photo_camera),
                    contentDescription = "Foto por defecto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isUploadingProfilePhoto) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (!isUploadingProfilePhoto) {
                    showOptions = true
                }
            },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
            enabled = !isUploadingProfilePhoto
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar foto de perfil",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = { Text("Seleccionar opción") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                showOptions = false
                                if (hasCameraPermission) {
                                    cameraLauncher.launch(null)
                                } else {
                                    requestCameraPermission.launch(cameraPermission)
                                }
                            }
                        ) {
                            Text("Tomar foto")
                        }

                        TextButton(
                            onClick = {
                                showOptions = false
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Text("Elegir de galería")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showOptions = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun MomentsGrid(model: ProfileViewModel) {
    val context = LocalContext.current
    val photos by model.momentPhotos.collectAsState()
    val isUploading by model.isUploadingMoment.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    val cameraPermission = Manifest.permission.CAMERA

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }


    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapToCacheUri(context, bitmap, "moment")?.let { uri ->
                model.uploadMomentPhoto(uri, isFromCamera = true)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            model.uploadMomentPhoto(uri, isFromCamera = false)
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) cameraLauncher.launch(null)
    }



    Row(
        Modifier
            .fillMaxWidth()
            .padding(13.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            "Mis Momentos",
            Modifier.weight(70f),
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = { showOptions = true },
            enabled = !isUploading,
            modifier = Modifier
                .size(30.dp)
                .weight(30f),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar foto")
        }
    }


    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Seleccionar opción") },
            text = {
                Column {

                    TextButton(
                        onClick = {
                            showOptions = false
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                requestCameraPermission.launch(cameraPermission)
                            }
                        }
                    ) {
                        Text("Tomar foto")
                    }


                    TextButton(
                        onClick = {
                            showOptions = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Text("Elegir de galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isUploading) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(
                text = " Subiendo foto...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (photos.isEmpty() && !isUploading) {
        Text(
            text = "Aun no tienes fotos en tus momentos",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(photos, key = { it }) { photoUrl ->
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

private fun bitmapToCacheUri(context: Context, bitmap: Bitmap, filePrefix: String): Uri? {
    return runCatching {
        val file = File(context.cacheDir, "${filePrefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        file.toUri()
    }.getOrNull()
}



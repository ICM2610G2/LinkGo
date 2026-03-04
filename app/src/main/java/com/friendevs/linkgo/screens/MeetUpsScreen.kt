package com.friendevs.linkgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.friendevs.linkgo.screens.MeetUpCard
import com.friendevs.linkgo.model.meetUpContacts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetUpsScreen(navController: NavController) {

    Scaffold(
        containerColor = Color(0xFF140F23),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Meet up", color = Color.White)
                        Text(
                            "Encuentra el lugar ideal",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F23)
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A5CFF)
                    )
                ) {
                    Text("Meet Up", color = Color.White)
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(meetUpContacts) { contact ->
                MeetUpCard(contact = contact)
            }

        }
    }
}
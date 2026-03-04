package com.friendevs.linkgo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.friendevs.linkgo.screens.ChatDetailScreen
import com.friendevs.linkgo.screens.ChatScreen
import com.friendevs.linkgo.screens.FeedScreen
import com.friendevs.linkgo.screens.HotspotsScreen
import com.friendevs.linkgo.screens.MapScreen
import com.friendevs.linkgo.screens.ProfileScreen


enum class Screens {

    Map,
    Feed,
    Chat,
    ChatDetail,
    Hotspots,
    Profile

}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Map.name) {
        composable(route = Screens.Map.name) {
            MapScreen(navController)
        }
        composable(route = Screens.Feed.name) {
            FeedScreen(navController)
        }
        composable(route = Screens.Chat.name) {
            ChatScreen(navController)
        }
        composable(route = Screens.ChatDetail.name) {
            ChatDetailScreen(navController)
        }
        composable(route = Screens.Hotspots.name) {
            HotspotsScreen(navController)
        }
        composable(route = Screens.Profile.name) {
            ProfileScreen(navController)
        }

    }
}
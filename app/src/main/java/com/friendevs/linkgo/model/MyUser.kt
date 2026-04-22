package com.friendevs.linkgo.model

data class MyUser(
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val age: String = "",
    val email: String = "",
    val friendsCount: String = "0",
    val postsCount: String = "0",
    val circlesCount: String = "0"
)

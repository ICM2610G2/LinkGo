package com.friendevs.linkgo.domain.model

data class User(
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val age: String = "",
    val email: String = "",
    val friendsCount: String = "0",
    val postsCount: String = "0",
    val circlesCount: String = "0"
)

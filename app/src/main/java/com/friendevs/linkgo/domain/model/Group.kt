package com.friendevs.linkgo.domain.model

data class GroupSummary(
    val id: String = "",
    val name: String = "",
    val descripcion: String = "",
    val codigoInvitacion: String = "",
    val members: Map<String, Boolean> = emptyMap()
) {
    val membersCount: Int
        get() = members.values.count { it }
}

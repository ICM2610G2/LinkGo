package com.friendevs.linkgo.ui.feature.chat

import androidx.lifecycle.ViewModel
import com.friendevs.linkgo.domain.model.GroupSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatState(
    val groups: List<GroupSummary> = emptyList()
)

class ChatViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChatState(groups = hardcodedGroups()))
    val state = _state.asStateFlow()

    fun loadUserGroups() {
        _state.value = ChatState(groups = hardcodedGroups())
    }

    private fun hardcodedGroups(): List<GroupSummary> {
        return listOf(
            GroupSummary(
                id = "grupo_001",
                name = "Ruta Norte",
                descripcion = "Salida dominical",
                codigoInvitacion = "RUTANORTE123",
                members = mapOf(
                    "uid_Andres" to true,
                    "uid_Oscar" to true,
                    "uid_Paula" to true
                )
            ),
            GroupSummary(
                id = "grupo_002",
                name = "Fotos en la ciudad",
                descripcion = "Sesion viernes",
                codigoInvitacion = "FOTOCIUDAD22",
                members = mapOf(
                    "uid_Andres" to true,
                    "uid_Laura" to true
                )
            ),
            GroupSummary(
                id = "grupo_003",
                name = "Cafe y codigo",
                descripcion = "Meet semanal",
                codigoInvitacion = "CODECAFE09",
                members = mapOf(
                    "uid_Andres" to true,
                    "uid_Juan" to true,
                    "uid_Maria" to true,
                    "uid_Carlos" to true
                )
            )
        )
    }
}

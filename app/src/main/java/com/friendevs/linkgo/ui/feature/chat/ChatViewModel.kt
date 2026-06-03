package com.friendevs.linkgo.ui.feature.chat

import androidx.lifecycle.ViewModel
import com.friendevs.linkgo.data.repository.GroupRepository
import com.friendevs.linkgo.domain.model.GroupSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatState(
    val groups: List<GroupSummary> = emptyList(),
    val selectedGroupId: String? = null
)

class ChatViewModel : ViewModel() {

    private val repo = GroupRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    private var groupsListener: ValueEventListener? = null

    init {
        loadUserGroups()
    }

    fun loadUserGroups() {
        val uid = auth.currentUser?.uid ?: return
        groupsListener = repo.observeMyGroups(uid) { groups ->
            val selected = _state.value.selectedGroupId?.takeIf { id -> groups.any { it.id == id } }
            _state.value = ChatState(groups = groups, selectedGroupId = selected)
        }
    }

    fun selectGroup(groupId: String?) {
        _state.value = _state.value.copy(selectedGroupId = groupId)
    }

    fun createGroup(name: String, descripcion: String) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank()) return
        repo.createGroup(uid, name.trim(), descripcion.trim())
    }

    fun joinGroup(code: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        if (code.isBlank()) return onResult(false)
        repo.joinByCode(uid, code.trim().uppercase(), onResult)
    }
}

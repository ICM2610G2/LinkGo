package com.friendevs.linkgo.ui.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.friendevs.linkgo.data.repository.ChatRepository
import com.friendevs.linkgo.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener

class ChatDetailViewModel : ViewModel() {

    private val repo = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    val currentUid: String? get() = auth.currentUser?.uid

    private var groupId: String = ""
    private var listener: ValueEventListener? = null

    fun start(groupId: String) {
        if (this.groupId == groupId && listener != null) return
        // limpiar listener previo si cambia el grupo
        listener?.let { repo.removeMessagesListener(this.groupId, it) }
        this.groupId = groupId
        listener = repo.observeMessages(groupId) { msgs ->
            messages = msgs
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || groupId.isEmpty()) return
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Yo"

        repo.sendMessage(
            groupId,
            Message(
                senderId = uid,
                senderName = name,
                text = trimmed,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        listener?.let { repo.removeMessagesListener(groupId, it) }
    }
}

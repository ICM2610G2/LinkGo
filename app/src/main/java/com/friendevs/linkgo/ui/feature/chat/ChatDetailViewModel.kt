package com.friendevs.linkgo.ui.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.friendevs.linkgo.data.repository.ChatRepository
import com.friendevs.linkgo.domain.model.GroupSummary
import com.friendevs.linkgo.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatDetailViewModel : ViewModel() {

    private val repo = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    var group by mutableStateOf<GroupSummary?>(null)
        private set

    val currentUid: String? get() = auth.currentUser?.uid

    var userPhotos by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    private var groupId: String = ""
    private var listener: ValueEventListener? = null
    private var groupListener: ValueEventListener? = null

    fun start(groupId: String) {
        if (this.groupId == groupId && listener != null) return
        // limpiar listener previo si cambia el grupo
        listener?.let { repo.removeMessagesListener(this.groupId, it) }
        groupListener?.let { db.child("groups").child(this.groupId).removeEventListener(it) }

        this.groupId = groupId
        listener = repo.observeMessages(groupId) { msgs ->
            messages = msgs
            val newSenderIds = msgs.map { it.senderId }.distinct().filter { it !in userPhotos.keys && it.isNotEmpty() }
            newSenderIds.forEach { senderId ->
                db.child("users").child(senderId).child("profilePhotoUrl").get().addOnSuccessListener { snapshot ->
                    val url = snapshot.getValue(String::class.java)
                    if (!url.isNullOrEmpty()) {
                        userPhotos = userPhotos + (senderId to url)
                    }
                }
            }
        }

        // Cargar datos del grupo (nombre, código de invitación, descripción)
        val gl = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                group = snapshot.getValue(GroupSummary::class.java)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        db.child("groups").child(groupId).addValueEventListener(gl)
        groupListener = gl
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
        groupListener?.let { db.child("groups").child(groupId).removeEventListener(it) }
    }
}

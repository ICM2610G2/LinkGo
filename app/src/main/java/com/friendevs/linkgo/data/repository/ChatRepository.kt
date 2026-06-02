package com.friendevs.linkgo.data.repository

import android.util.Log
import com.friendevs.linkgo.domain.model.Message
import com.google.firebase.database.*

class ChatRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun sendMessage(groupId: String, message: Message) {
        val msgRef = db.child("chats").child(groupId).child("messages").push()

        val msgMap = mapOf(
            "id" to msgRef.key,
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "text" to message.text,
            "timestamp" to message.timestamp
        )

        msgRef.setValue(msgMap)
    }

    /**
     * Escucha en tiempo real los mensajes de un grupo.
     * Devuelve el listener para poder removerlo luego.
     */
    fun observeMessages(groupId: String, onResult: (List<Message>) -> Unit): ValueEventListener {
        val ref = db.child("chats").child(groupId).child("messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()

                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    message?.let { list.add(it) }
                }

                onResult(list.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "observeMessages cancelled: ${error.message}")
            }
        }

        ref.addValueEventListener(listener)
        return listener
    }

    fun removeMessagesListener(groupId: String, listener: ValueEventListener) {
        db.child("chats").child(groupId).child("messages").removeEventListener(listener)
    }
}

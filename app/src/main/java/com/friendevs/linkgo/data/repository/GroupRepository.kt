package com.friendevs.linkgo.data.repository

import android.util.Log
import com.friendevs.linkgo.domain.model.GroupSummary
import com.google.firebase.database.*

class GroupRepository {

    private val db = FirebaseDatabase.getInstance().reference

    /**
     * Escucha los grupos de los que el usuario es miembro.
     * Lee /groups y filtra por members/{uid} == true.
     */
    fun observeMyGroups(uid: String, onResult: (List<GroupSummary>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<GroupSummary>()

                for (child in snapshot.children) {
                    val group = child.getValue(GroupSummary::class.java)
                    if (group != null && group.members[uid] == true) {
                        list.add(group)
                    }
                }

                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GroupRepository", "observeMyGroups cancelled: ${error.message}")
            }
        }

        db.child("groups").addValueEventListener(listener)
        return listener
    }

    fun createGroup(uid: String, name: String, descripcion: String) {
        val groupRef = db.child("groups").push()
        val groupId = groupRef.key ?: return
        val code = generateCode()

        val groupMap = mapOf(
            "id" to groupId,
            "name" to name,
            "descripcion" to descripcion,
            "codigoInvitacion" to code,
            "members" to mapOf(uid to true)
        )

        groupRef.setValue(groupMap)
        db.child("users").child(uid).child("groups").child(groupId).setValue(true)
    }

    /**
     * Busca un grupo por su codigo de invitacion y agrega al usuario como miembro.
     * onResult(true) si se unio, onResult(false) si el codigo no existe.
     */
    fun joinByCode(uid: String, code: String, onResult: (Boolean) -> Unit) {
        db.child("groups")
            .orderByChild("codigoInvitacion")
            .equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupSnap = snapshot.children.firstOrNull()
                    if (groupSnap == null) {
                        onResult(false)
                        return
                    }

                    val groupId = groupSnap.key ?: return onResult(false)
                    db.child("groups").child(groupId).child("members").child(uid).setValue(true)
                    db.child("users").child(uid).child("groups").child(groupId).setValue(true)
                    onResult(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GroupRepository", "joinByCode cancelled: ${error.message}")
                    onResult(false)
                }
            })
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}

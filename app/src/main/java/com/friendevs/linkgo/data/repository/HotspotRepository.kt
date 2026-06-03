package com.friendevs.linkgo.data.repository

import com.friendevs.linkgo.domain.model.Hotspot
import com.google.firebase.database.*

class FirebaseHotspotRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun saveHotspot(userId: String, groupId: String, hotspot: Hotspot) {
        val hotspotRef = db.child("hotspots").push()

        val hotspotMap = mapOf(
            "id" to hotspotRef.key,
            "name" to hotspot.name,
            "lat" to hotspot.lat,
            "lng" to hotspot.lng,
            "address" to hotspot.address,
            "groupId" to groupId,
            "creatorId" to userId,
            "fotos" to hotspot.fotos,
            "url" to hotspot.url
        )

        hotspotRef.setValue(hotspotMap)

        db.child("users")
            .child(userId)
            .child("hotspots")
            .child(hotspotRef.key!!)
            .setValue(true)
    }

    fun getHotspots(onResult: (List<Hotspot>) -> Unit) {
        db.child("hotspots").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Hotspot>()

                for (child in snapshot.children) {
                    val hotspot = child.getValue(Hotspot::class.java)
                    hotspot?.let { list.add(it) }
                }

                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getHotspotsByUser(userId: String, onResult: (List<Hotspot>) -> Unit) {
        db.child("hotspots")
            .orderByChild("creatorId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Hotspot>()

                    for (child in snapshot.children) {
                        val hotspot = child.getValue(Hotspot::class.java)
                        hotspot?.let { list.add(it) }
                    }

                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getHotspotsByGroup(groupId: String, onResult: (List<Hotspot>) -> Unit) {
        db.child("hotspots")
            .orderByChild("groupId")
            .equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Hotspot>()

                    for (child in snapshot.children) {
                        val hotspot = child.getValue(Hotspot::class.java)
                        hotspot?.let { list.add(it) }
                    }

                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
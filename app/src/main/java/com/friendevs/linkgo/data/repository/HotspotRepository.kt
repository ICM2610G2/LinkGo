package com.friendevs.linkgo.data.repository

import com.friendevs.linkgo.domain.model.Hotspot
import com.google.firebase.database.*

class FirebaseHotspotRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun saveHotspot(userId: String, hotspot: Hotspot) {
        val hotspotRef = db.child("hotspots").push()

        val hotspotMap = mapOf(
            "id" to hotspotRef.key,
            "name" to hotspot.name,
            "lat" to hotspot.lat,
            "lng" to hotspot.lng,
            "address" to hotspot.address,
            "creatorId" to userId,
            "fotos" to hotspot.fotos,
            "url" to hotspot.url
        )

        hotspotRef.setValue(hotspotMap)

        // 🔥 relación con usuario
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
}
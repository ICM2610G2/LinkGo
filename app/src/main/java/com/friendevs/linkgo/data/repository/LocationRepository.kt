package com.friendevs.linkgo.data.repository

import android.util.Log
import com.friendevs.linkgo.domain.model.UserLocation
import com.google.firebase.database.*

class LocationRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun writeLocation(uid: String, lat: Double, lng: Double) {
        val locMap = mapOf(
            "lat" to lat,
            "lng" to lng,
            "updatedAt" to System.currentTimeMillis()
        )
        db.child("users").child(uid).child("location").setValue(locMap)
    }

    /**
     * Escucha /users y construye un mapa uid -> UserLocation para los usuarios
     * que tengan un nodo location valido. El filtrado por grupo se hace en el ViewModel.
     */
    fun observeAllLocations(onResult: (Map<String, UserLocation>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableMapOf<String, UserLocation>()

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val locSnap = child.child("location")
                    val lat = locSnap.child("lat").getValue(Double::class.java)
                    val lng = locSnap.child("lng").getValue(Double::class.java)

                    if (lat != null && lng != null) {
                        val name = child.child("name").getValue(String::class.java) ?: "Usuario"
                        result[uid] = UserLocation(uid = uid, name = name, lat = lat, lng = lng)
                    }
                }

                onResult(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LocationRepository", "observeAllLocations cancelled: ${error.message}")
            }
        }

        db.child("users").addValueEventListener(listener)
        return listener
    }
}

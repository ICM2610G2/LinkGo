package com.friendevs.linkgo.data.repository

import android.util.Log
import com.friendevs.linkgo.domain.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LocationRepository {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun writeLocation(uid: String, lat: Double, lng: Double) {
        val currentUser = auth.currentUser
        val locMap = mapOf(
            "lat" to lat,
            "lng" to lng,
            "updatedAt" to System.currentTimeMillis(),
            "name" to (currentUser?.displayName ?: "Usuario"),
            "profilePhotoUrl" to (currentUser?.photoUrl?.toString() ?: "")
        )
        db.child("locations").child(uid).updateChildren(locMap)
        db.child("users").child(uid).child("location").updateChildren(
            mapOf(
                "lat" to lat,
                "lng" to lng,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    fun updateLocationProfile(uid: String, name: String, profilePhotoUrl: String) {
        val payload = mapOf(
            "name" to name,
            "profilePhotoUrl" to profilePhotoUrl
        )
        db.child("locations").child(uid).updateChildren(payload)
    }

    private fun DataSnapshot.readDouble(field: String): Double? {
        val direct = child(field).value
        return when (direct) {
            is Number -> direct.toDouble()
            is String -> direct.toDoubleOrNull()
            else -> null
        } ?: run {
            val nested = child("location").child(field).value
            when (nested) {
                is Number -> nested.toDouble()
                is String -> nested.toDoubleOrNull()
                else -> null
            }
        }
    }

    /**
     * Escucha /locations y construye un mapa uid -> UserLocation.
     * El filtrado por grupo se hace en el ViewModel.
     */
    fun observeAllLocations(onResult: (Map<String, UserLocation>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableMapOf<String, UserLocation>()

                Log.d("LocationRepository", "observeAllLocations.onDataChange: snapshot has ${snapshot.children.count()} children")

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val lat = child.readDouble("lat")
                    val lng = child.readDouble("lng")

                    Log.d("LocationRepository", "Processing uid=$uid: lat=$lat, lng=$lng")

                    if (lat != null && lng != null) {
                        val name = child.child("name").getValue(String::class.java)
                            ?: child.child("fullName").getValue(String::class.java)
                            ?: "Usuario"
                        val profilePhotoUrl = child.child("profilePhotoUrl").getValue(String::class.java).orEmpty()
                        Log.d("LocationRepository", "  ✓ Created UserLocation for $uid: name=$name, photoUrl=$profilePhotoUrl")
                        result[uid] = UserLocation(
                            uid = uid,
                            name = name,
                            lat = lat,
                            lng = lng,
                            profilePhotoUrl = profilePhotoUrl
                        )
                    } else {
                        Log.d("LocationRepository", "  ✗ Skipped $uid: lat or lng is null")
                    }
                }

                db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        result.forEach { (uid, location) ->
                            val userSnapshot = usersSnapshot.child(uid)
                            val fallbackName = userSnapshot.child("name").getValue(String::class.java)
                                ?: userSnapshot.child("fullName").getValue(String::class.java)
                            val fallbackPhoto = userSnapshot.child("profilePhotoUrl").getValue(String::class.java)

                            if ((location.name.isBlank() || location.profilePhotoUrl.isBlank()) &&
                                (fallbackName != null || !fallbackPhoto.isNullOrBlank())
                            ) {
                                result[uid] = location.copy(
                                    name = location.name.ifBlank { fallbackName ?: location.name },
                                    profilePhotoUrl = location.profilePhotoUrl.ifBlank { fallbackPhoto.orEmpty() }
                                )
                            }
                        }

                        Log.d("LocationRepository", "observeAllLocations.onDataChange: Returning ${result.size} locations")
                        onResult(result)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LocationRepository", "users fallback cancelled: ${error.message}")
                        Log.d("LocationRepository", "observeAllLocations.onDataChange: Returning ${result.size} locations without users fallback")
                        onResult(result)
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LocationRepository", "observeAllLocations cancelled: ${error.message}")
            }
        }

        Log.d("LocationRepository", "observeAllLocations: Adding ValueEventListener to /locations")
        db.child("locations").addValueEventListener(listener)
        return listener
    }
}

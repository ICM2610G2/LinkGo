package com.friendevs.linkgo.data.repository

import android.content.Context
import com.friendevs.linkgo.domain.model.Hotspot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

fun loadHotspots(context: Context): List<Hotspot> {
    val file = File(context.filesDir, "hotspots.json")

    if (!file.exists()) {
        val json = context.assets.open("hotSpots/HotSpots.json")
            .bufferedReader().use { it.readText() }
        file.writeText(json)
    }

    val json = file.readText()
    val type = object : TypeToken<List<Hotspot>>() {}.type
    return Gson().fromJson(json, type)
}

fun saveHotspot(context: Context, newHotspot: Hotspot) {
    val current = loadHotspots(context).toMutableList()
    current.add(newHotspot)
    val file = File(context.filesDir, "hotspots.json")
    file.writeText(Gson().toJson(current))
}
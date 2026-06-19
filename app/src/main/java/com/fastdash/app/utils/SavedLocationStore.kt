package com.fastdash.app.utils

import android.content.Context
import com.fastdash.app.ui.checkout.PickedLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SavedLocationStore(context: Context) {
    private val prefs = context.getSharedPreferences("fastdash_saved_locations", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getLocations(): List<PickedLocation> {
        val raw = prefs.getString(KEY_LOCATIONS, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        val type = object : TypeToken<List<PickedLocation>>() {}.type
        return runCatching { gson.fromJson<List<PickedLocation>>(raw, type).orEmpty() }.getOrDefault(emptyList())
    }

    fun saveLocations(locations: List<PickedLocation>) {
        prefs.edit().putString(KEY_LOCATIONS, gson.toJson(locations.take(MAX_LOCATIONS))).apply()
    }

    fun addLocation(location: PickedLocation) {
        val merged = listOf(location) + getLocations().filterNot {
            it.latitude == location.latitude &&
                it.longitude == location.longitude &&
                it.address == location.address
        }
        saveLocations(merged)
    }

    companion object {
        private const val KEY_LOCATIONS = "locations"
        private const val MAX_LOCATIONS = 10
    }
}

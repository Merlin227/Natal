// HoroscopeCacheManager.kt
package com.example.natal

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HoroscopeCacheManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("horoscope_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HOROSCOPES = "horoscopes_data"
        private const val KEY_LAST_UPDATE = "last_update_date"
        private const val CACHE_DURATION_HOURS = 12
    }

    fun saveHoroscopes(horoscopes: List<HoroscopeData>) {
        val json = gson.toJson(horoscopes)
        sharedPrefs.edit()
            .putString(KEY_HOROSCOPES, json)
            .putString(KEY_LAST_UPDATE, getCurrentDate())
            .apply()
    }

    fun getCachedHoroscopes(): List<HoroscopeData>? {
        val json = sharedPrefs.getString(KEY_HOROSCOPES, null)
        return if (json != null) {
            val type = object : TypeToken<List<HoroscopeData>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun isCacheValid(): Boolean {
        val lastUpdate = sharedPrefs.getString(KEY_LAST_UPDATE, null)
        if (lastUpdate == null) return false

        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val lastUpdateDate = dateFormat.parse(lastUpdate)
            val currentDate = dateFormat.parse(getCurrentDate())

            val diff = Calendar.getInstance().apply {
                time = lastUpdateDate
            }.let { lastCal ->
                Calendar.getInstance().apply {
                    time = currentDate
                }.let { currentCal ->
                    currentCal.timeInMillis - lastCal.timeInMillis
                }
            }

            diff <= CACHE_DURATION_HOURS * 60 * 60 * 1000
        } catch (e: Exception) {
            false
        }
    }

    fun getLastUpdateDate(): String? {
        return sharedPrefs.getString(KEY_LAST_UPDATE, null)
    }

    fun clearCache() {
        sharedPrefs.edit().clear().apply()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
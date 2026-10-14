package com.example.data.meter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Meter readings the user types in from the meter display or their vendor app. Kept only on
 * this phone. These are the only "server readings" Bright shows without a real gateway, and they
 * are always labelled as entered by the user.
 */
data class ManualMeterReading(
    val takenAt: Long,
    val creditKwh: Double?,
    val totalKwh: Double?,
    val note: String
)

object ManualMeterReadings {
    private const val FILE = "bright_manual_meter_readings"
    private const val KEY = "readings"

    fun all(context: Context): List<ManualMeterReading> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ManualMeterReading(
                    takenAt = o.getLong("takenAt"),
                    creditKwh = if (o.has("creditKwh")) o.getDouble("creditKwh") else null,
                    totalKwh = if (o.has("totalKwh")) o.getDouble("totalKwh") else null,
                    note = o.optString("note", "")
                )
            }.sortedByDescending { it.takenAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, reading: ManualMeterReading) {
        save(context, listOf(reading) + all(context))
    }

    fun remove(context: Context, reading: ManualMeterReading) {
        save(context, all(context).filterNot { it == reading })
    }

    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun save(context: Context, list: List<ManualMeterReading>) {
        val arr = JSONArray()
        list.take(500).forEach { r ->
            arr.put(JSONObject().apply {
                put("takenAt", r.takenAt)
                r.creditKwh?.let { put("creditKwh", it) }
                r.totalKwh?.let { put("totalKwh", it) }
                put("note", r.note)
            })
        }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }
}

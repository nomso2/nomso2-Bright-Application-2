package com.example.data.meter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** STS prepaid tokens: always exactly 20 digits. */
object StsToken {
    fun digits(input: String): String = input.filter { it.isDigit() }
    fun isValid(input: String): Boolean = digits(input).length == 20 && input.all { it.isDigit() || it == ' ' || it == '-' }
    fun pretty(input: String): String = digits(input).chunked(4).joinToString(" ")

    /** Keypad steps: the same for every STS meter, with the brand's usual confirm key. */
    fun keypadSteps(token: String, brand: String?): List<String> {
        val groups = digits(token).chunked(4)
        return buildList {
            add("Make sure the meter shows the normal screen (credit or kWh).")
            groups.forEachIndexed { i, g -> add("Type ${g.toCharArray().joinToString(" ")}  (group ${i + 1} of 5)") }
            add("Press the Enter key (marked ↵, # or 'Enter'${brand?.let { " on your $it meter" } ?: ""}).")
            add("Wait for 'Accept' / 'Success'. If it shows 'Reject' or 'Used', check the meter number on your receipt.")
        }
    }
}

/** A token the user entered, with what happened to it. Real records only, kept on this phone. */
data class TokenRecord(
    val token: String,
    val meterNumber: String,
    val enteredAt: Long,
    /** KEYPAD (user typed it on the meter), HES (pushed to a real HES), DEMO (pushed to the demo meter) */
    val method: String,
    val result: String
)

object TokenHistory {
    private const val FILE = "bright_token_history"
    private const val KEY = "tokens"

    fun all(context: Context): List<TokenRecord> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null) ?: return emptyList()
        return try {
            val a = JSONArray(raw)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                TokenRecord(o.getString("token"), o.optString("meter"), o.getLong("at"), o.optString("method"), o.optString("result"))
            }.sortedByDescending { it.enteredAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, r: TokenRecord) {
        val list = (listOf(r) + all(context)).take(300)
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().put("token", it.token).put("meter", it.meterNumber).put("at", it.enteredAt)
                .put("method", it.method).put("result", it.result))
        }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, a.toString()).apply()
    }

    fun clear(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
}

/** The meter the user is connecting: number + manufacturer (picked or read from the nameplate). */
object MeterIdentity {
    private const val FILE = "bright_meter_identity"

    fun meterNumber(context: Context, fallback: String): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("meter", null)?.takeIf { it.isNotBlank() } ?: fallback

    fun manufacturer(context: Context): com.example.model.MeterManufacturer? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("mfg", null)
            ?.let { n -> com.example.model.MeterManufacturer.entries.firstOrNull { it.name == n } }

    fun save(context: Context, meterNumber: String, manufacturer: com.example.model.MeterManufacturer?) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString("meter", meterNumber)
            .putString("mfg", manufacturer?.name)
            .apply()
    }

    fun clear(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
}

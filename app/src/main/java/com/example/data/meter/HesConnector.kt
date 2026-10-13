package com.example.data.meter

import android.content.Context
import com.example.model.MeterManufacturer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

/**
 * A head-end system (HES / MDMS) connection for one DisCo + manufacturer. A real connector
 * needs a DisCo-issued endpoint and key. Until then a clearly labelled DEMO connector stands in.
 */
interface HesConnector {
    val isDemo: Boolean
    val title: String
    suspend fun readTelemetry(meterNumber: String): MeterResult<MeterReading>
    /** Times one request to the endpoint. Returns milliseconds (app-to-server, not meter latency). */
    suspend fun ping(): MeterResult<Long>
    /** Pushes a 20-digit STS token. Returns credit after loading when the server says. */
    suspend fun pushToken(meterNumber: String, token: String): MeterResult<Double?>
}

data class HesConfig(val baseUrl: String, val apiKey: String)

/**
 * Generic REST adapter. Contract (configure your DisCo's HES / MDMS gateway to match, or add an
 * adapter for its own API):
 *   GET  {base}/meters/{meter}/telemetry   -> JSON {creditKwh, totalImportKwh, activePowerW, voltageV,
 *                                              currentA, powerFactor, frequencyHz, relay, tamper[]}
 *   POST {base}/meters/{meter}/tokens       {"token": "<20 digits>"} -> {accepted, creditKwh}
 *   Authorization: Bearer <api key>
 * Missing fields stay empty. Nothing is filled with defaults.
 */
class RestHesConnector(
    private val manufacturer: MeterManufacturer,
    private val discoCode: String,
    private val config: HesConfig
) : HesConnector {
    override val isDemo = false
    override val title = "${manufacturer.displayName} via $discoCode HES"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val base get() = config.baseUrl.trim().trimEnd('/')

    private fun request(path: String) = Request.Builder()
        .url(base + path)
        .header("Authorization", "Bearer ${config.apiKey}")
        .header("Accept", "application/json")
        .header("X-DisCo-Code", discoCode)
        .header("X-Meter-Manufacturer", manufacturer.name)

    override suspend fun readTelemetry(meterNumber: String): MeterResult<MeterReading> = withContext(Dispatchers.IO) {
        try {
            client.newCall(request("/meters/$meterNumber/telemetry").get().build()).execute().use { r ->
                if (!r.isSuccessful) return@withContext MeterResult.Fail("HES answered HTTP ${r.code}")
                val body = r.body?.string().orEmpty()
                MeterResult.Ok(parseTelemetry(JSONObject(body), meterNumber))
            }
        } catch (e: Exception) {
            MeterResult.Fail("Couldn't reach the HES: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    override suspend fun ping(): MeterResult<Long> = withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        try {
            val code = client.newCall(request("").head().build()).execute().use { r -> r.code }
            val ms = (System.nanoTime() - start) / 1_000_000
            if (code >= 500) MeterResult.Fail("HES answered HTTP $code after $ms ms") else MeterResult.Ok(ms)
        } catch (e: Exception) {
            MeterResult.Fail("No answer: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    override suspend fun pushToken(meterNumber: String, token: String): MeterResult<Double?> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().put("token", token).toString()
            client.newCall(
                request("/meters/$meterNumber/tokens").post(json.toRequestBody("application/json".toMediaType())).build()
            ).execute().use { r ->
                val body = r.body?.string().orEmpty()
                if (!r.isSuccessful) return@withContext MeterResult.Fail("HES rejected the token (HTTP ${r.code})")
                val o = runCatching { JSONObject(body) }.getOrNull()
                if (o != null && o.has("accepted") && !o.optBoolean("accepted")) {
                    return@withContext MeterResult.Fail(o.optString("message", "HES rejected the token"))
                }
                MeterResult.Ok(o?.let { if (it.has("creditKwh")) it.optDouble("creditKwh") else null })
            }
        } catch (e: Exception) {
            MeterResult.Fail("Couldn't reach the HES: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun parseTelemetry(o: JSONObject, meterNumber: String): MeterReading {
        fun d(k: String) = if (o.has(k) && !o.isNull(k)) o.optDouble(k).takeIf { !it.isNaN() } else null
        val relay = when (o.optString("relay").uppercase()) {
            "CONNECTED", "CLOSED", "ON" -> RelayStatus.CONNECTED
            "TRIPPED_OVERLOAD", "TRIPPED", "OVERLOAD" -> RelayStatus.TRIPPED_OVERLOAD
            "OPEN_ZERO_BALANCE", "ZERO_BALANCE", "NO_CREDIT" -> RelayStatus.OPEN_ZERO_BALANCE
            "OPEN", "DISCONNECTED", "OFF" -> RelayStatus.OPEN_OTHER
            else -> RelayStatus.UNKNOWN
        }
        val tamperArr = o.optJSONArray("tamper")
        val flags = mutableSetOf<TamperFlag>()
        if (tamperArr != null) {
            for (i in 0 until tamperArr.length()) {
                when (tamperArr.optString(i).uppercase()) {
                    "TERMINAL_COVER", "COVER_OPEN" -> flags += TamperFlag.TERMINAL_COVER
                    "MAGNETIC" -> flags += TamperFlag.MAGNETIC
                    "REVERSE_ENERGY", "REVERSE" -> flags += TamperFlag.REVERSE_ENERGY
                    "NEUTRAL_MISSING", "NEUTRAL" -> flags += TamperFlag.NEUTRAL_MISSING
                    "FAULT", "METER_FAULT" -> flags += TamperFlag.METER_FAULT
                }
            }
        }
        return MeterReading(
            source = ReadingSource.HES,
            meterNumber = meterNumber,
            takenAt = System.currentTimeMillis(),
            creditKwh = d("creditKwh"),
            totalImportKwh = d("totalImportKwh"),
            totalExportKwh = d("totalExportKwh"),
            activePowerW = d("activePowerW") ?: d("activePowerKw")?.times(1000),
            voltageV = d("voltageV"),
            currentA = d("currentA"),
            powerFactor = d("powerFactor"),
            frequencyHz = d("frequencyHz"),
            relay = relay,
            tamperFlags = flags,
            tamperReported = tamperArr != null
        )
    }
}

/**
 * DEMO connector: simulated meter for one manufacturer so the screens can be tried before a
 * DisCo grants HES access. Every value it returns is invented and marked ReadingSource.DEMO.
 * The UI must show a DEMO badge wherever these values appear.
 */
class DemoHesConnector(private val manufacturer: MeterManufacturer) : HesConnector {
    override val isDemo = true
    override val title = "${manufacturer.displayName} DEMO meter"

    private val state get() = DemoMeterStates.forManufacturer(manufacturer)

    override suspend fun readTelemetry(meterNumber: String): MeterResult<MeterReading> {
        delay(Random.nextLong(150, 450))
        val s = state
        s.tick()
        return MeterResult.Ok(
            MeterReading(
                source = ReadingSource.DEMO,
                meterNumber = meterNumber.ifBlank { "DEMO-${manufacturer.name}" },
                takenAt = System.currentTimeMillis(),
                creditKwh = s.creditKwh,
                totalImportKwh = s.totalKwh,
                activePowerW = if (s.relay == RelayStatus.CONNECTED) s.loadW else 0.0,
                voltageV = s.voltage,
                currentA = if (s.relay == RelayStatus.CONNECTED) s.currentA else 0.0,
                powerFactor = s.pf,
                frequencyHz = s.frequency,
                relay = s.relay,
                tamperFlags = s.tamper,
                tamperReported = true,
                meterIdentification = "DEMO ${manufacturer.displayName}"
            )
        )
    }

    override suspend fun ping(): MeterResult<Long> {
        val ms = Random.nextLong(60, 420)
        delay(ms)
        return MeterResult.Ok(ms)
    }

    override suspend fun pushToken(meterNumber: String, token: String): MeterResult<Double?> {
        delay(Random.nextLong(300, 900))
        if (!StsToken.isValid(token)) return MeterResult.Fail("A token is exactly 20 digits.")
        val added = 10.0 + abs(token.hashCode() % 50)
        return MeterResult.Ok(state.credit(added))
    }
}

/** In-memory simulated meters, one per manufacturer (reset when the app restarts). */
object DemoMeterStates {
    private val states = mutableMapOf<MeterManufacturer, DemoMeterState>()

    @Synchronized
    fun forManufacturer(m: MeterManufacturer): DemoMeterState =
        states.getOrPut(m) { DemoMeterState(seed = m.ordinal) }
}

class DemoMeterState(seed: Int) {
    private val rnd = Random(seed * 7919 + System.currentTimeMillis().toInt())
    var creditKwh = 18.0 + rnd.nextDouble() * 20
        private set
    var totalKwh = 900.0 + rnd.nextDouble() * 1500
        private set
    var loadW = 600.0
        private set
    var voltage = 225.0
        private set
    var currentA = 2.7
        private set
    var pf = 0.93
        private set
    var frequency = 50.0
        private set
    var relay = RelayStatus.CONNECTED
        private set
    var tamper: Set<TamperFlag> = emptySet()
        private set
    private var lastTick = System.currentTimeMillis()
    private var trippedUntil = 0L

    @Synchronized
    fun tick() {
        val now = System.currentTimeMillis()
        val hours = ((now - lastTick).coerceAtMost(10 * 60_000L)) / 3_600_000.0
        lastTick = now
        voltage = (205.0 + rnd.nextDouble() * 30).round(1)
        loadW = (250.0 + rnd.nextDouble() * 2600).round(0)
        pf = (0.82 + rnd.nextDouble() * 0.16).round(2)
        frequency = (49.85 + rnd.nextDouble() * 0.3).round(2)
        currentA = (loadW / (voltage * pf)).round(2)
        if (relay == RelayStatus.CONNECTED) {
            val used = loadW / 1000.0 * hours * 60 // sped up so the demo visibly moves
            creditKwh = (creditKwh - used).coerceAtLeast(0.0).round(2)
            totalKwh = (totalKwh + used).round(2)
        }
        relay = when {
            creditKwh <= 0.0 -> RelayStatus.OPEN_ZERO_BALANCE
            now < trippedUntil -> RelayStatus.TRIPPED_OVERLOAD
            loadW > 2700 -> { trippedUntil = now + 20_000; RelayStatus.TRIPPED_OVERLOAD }
            else -> RelayStatus.CONNECTED
        }
        val flags = mutableSetOf<TamperFlag>()
        if (rnd.nextInt(25) == 0) flags += TamperFlag.TERMINAL_COVER
        if (rnd.nextInt(30) == 0) flags += TamperFlag.MAGNETIC
        if (rnd.nextInt(40) == 0) flags += TamperFlag.REVERSE_ENERGY
        if (rnd.nextInt(35) == 0) flags += TamperFlag.NEUTRAL_MISSING
        tamper = flags
    }

    @Synchronized
    fun credit(kwh: Double): Double {
        creditKwh = (creditKwh + kwh).round(2)
        if (relay == RelayStatus.OPEN_ZERO_BALANCE) relay = RelayStatus.CONNECTED
        return creditKwh
    }

    private fun Double.round(places: Int): Double {
        var f = 1.0
        repeat(places) { f *= 10 }
        return Math.round(this * f) / f
    }
}

/**
 * Where HES connections are configured (base URL in prefs, API key encrypted in the Keystore)
 * and which connector each meter uses: a configured real HES replaces DEMO for that
 * DisCo + manufacturer.
 */
object HesRegistry {
    private const val PREFS = "bright_hes_config"

    private fun id(discoCode: String, m: MeterManufacturer) = "${discoCode.uppercase()}:${m.name}"

    fun config(context: Context, discoCode: String, m: MeterManufacturer): HesConfig? {
        val url = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("url:" + id(discoCode, m), null)
            ?.takeIf { it.startsWith("https://") || it.startsWith("http://") } ?: return null
        val key = SecureStore(context).get("key:" + id(discoCode, m)) ?: return null
        return HesConfig(url, key)
    }

    fun save(context: Context, discoCode: String, m: MeterManufacturer, baseUrl: String, apiKey: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("url:" + id(discoCode, m), baseUrl.trim()).apply()
        SecureStore(context).put("key:" + id(discoCode, m), apiKey.trim())
    }

    fun remove(context: Context, discoCode: String, m: MeterManufacturer) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("url:" + id(discoCode, m)).apply()
        SecureStore(context).remove("key:" + id(discoCode, m))
    }

    fun savedUrl(context: Context, discoCode: String, m: MeterManufacturer): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("url:" + id(discoCode, m), "") ?: ""

    /** Real HES when configured, otherwise the DEMO meter for that manufacturer. */
    fun connectorFor(context: Context, discoCode: String, m: MeterManufacturer): HesConnector =
        config(context, discoCode, m)?.let { RestHesConnector(m, discoCode, it) } ?: DemoHesConnector(m)

    /** Delete account. */
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        SecureStore(context).clear()
    }
}

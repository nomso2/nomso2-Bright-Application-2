package com.example.data.cloud

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.database.GridTelemetryEntity
import com.example.model.GridTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * National grid figures from NISO's public homepage (https://www.niso.org.ng/), which publishes
 * "Grid Performance for <date>" once a day: peak generation (MW, time, frequency), off-peak
 * generation, energy generated and energy sent out (MWh).
 *
 * These are DAILY figures for the previous day, not a live reading. Nothing here is invented:
 * if the page can't be fetched or parsed, the last saved figures are returned with their date,
 * or nothing at all.
 */
object NisoGridFeed {
    const val SOURCE_URL = "https://www.niso.org.ng/"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /** Fetches and caches today's figures; on failure returns the cached copy (or empty). */
    suspend fun refresh(context: Context): GridTelemetry = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).gridTelemetryDao()
        val fresh = try {
            fetch()
        } catch (e: Exception) {
            null
        }
        if (fresh != null) {
            dao.cacheTelemetry(GridTelemetryEntity.fromDomain(fresh))
            fresh
        } else {
            dao.getTelemetrySync()?.toDomain(fromCache = true) ?: GridTelemetry()
        }
    }

    /** Last saved figures without touching the network. */
    suspend fun cached(context: Context): GridTelemetry = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).gridTelemetryDao().getTelemetrySync()?.toDomain(fromCache = true)
            ?: GridTelemetry()
    }

    private fun fetch(): GridTelemetry? {
        val request = Request.Builder()
            .url(SOURCE_URL)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android) BrightApp")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val html = response.body?.string() ?: return null
            return parse(html, System.currentTimeMillis())
        }
    }

    /** Parses the homepage HTML. Returns null unless at least the date and peak figure are found. */
    fun parse(html: String, fetchedAt: Long): GridTelemetry? {
        val text = html
            .replace(Regex("(?is)<script.*?</script>"), " ")
            .replace(Regex("(?is)<style.*?</style>"), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("\\", "")
            .replace(Regex("\\s+"), " ")

        val start = text.indexOf("Grid Performance for")
        if (start < 0) return null
        val section = text.substring(start, minOf(text.length, start + 1500))

        val date = Regex("Grid Performance for\\s*:*\\s*([A-Za-z]+,?\\s+[A-Za-z]+\\s+\\d{1,2},?\\s+\\d{4})")
            .find(section)?.groupValues?.get(1)?.trim() ?: return null

        fun num(s: String?) = s?.replace(",", "")?.toDoubleOrNull()
        val genPattern = "Generation\\s*([\\d,]+(?:\\.\\d+)?)\\s*MW\\s*Time:\\s*([\\d:]+)\\s*Hrs?\\s*\\|?\\s*Freq\\.?:?\\s*([\\d.]+)\\s*Hz"
        val peak = Regex("(?<!Off-)Peak\\s*$genPattern").find(section) ?: return null
        val offPeak = Regex("Off-Peak\\s*$genPattern").find(section)
        val generated = Regex("Energy\\s*Generated\\s*([\\d,]+(?:\\.\\d+)?)\\s*MWh").find(section)
        val sentOut = Regex("Energy\\s*Sent\\s*Out\\s*([\\d,]+(?:\\.\\d+)?)\\s*MWh").find(section)

        return GridTelemetry(
            reportDate = date,
            peakGenerationMw = num(peak.groupValues[1]) ?: return null,
            peakTime = peak.groupValues[2],
            peakFrequencyHz = num(peak.groupValues[3]),
            offPeakGenerationMw = num(offPeak?.groupValues?.get(1)),
            offPeakTime = offPeak?.groupValues?.get(2) ?: "",
            offPeakFrequencyHz = num(offPeak?.groupValues?.get(3)),
            energyGeneratedMwh = num(generated?.groupValues?.get(1)),
            energySentOutMwh = num(sentOut?.groupValues?.get(1)),
            source = "NISO",
            fetchedAt = fetchedAt,
            isFromCache = false
        )
    }
}

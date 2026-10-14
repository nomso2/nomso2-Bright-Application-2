package com.example.data.meter

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

/**
 * Reads a meter through a USB-OTG optical (IR) probe using IEC 62056-21 mode C data readout:
 * open at 300 baud 7E1, send "/?!", read the identification "/XXXZ...", acknowledge with
 * ACK 0 Z 0 to switch to the meter's baud rate, then read the data block (STX ... ! CR LF ETX BCC).
 * Mode B meters (letter baud codes) switch baud without the ACK.
 *
 * Only values the meter actually sends are returned; the raw lines are kept for checking.
 */
object Iec62056Reader {
    private const val ACTION_USB_PERMISSION = "com.example.bright.USB_PERMISSION"
    private const val STX: Byte = 0x02
    private const val ETX: Byte = 0x03
    private const val ACK: Byte = 0x06

    /** Optical probes / USB-serial adapters currently plugged in. */
    fun findProbes(context: Context): List<UsbSerialDriver> {
        val usb = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return emptyList()
        return try {
            UsbSerialProber.getDefaultProber().findAllDrivers(usb)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun describe(driver: UsbSerialDriver): String {
        val d = driver.device
        val name = d.productName ?: d.deviceName
        return "$name (VID ${"%04X".format(d.vendorId)} / PID ${"%04X".format(d.productId)})"
    }

    private suspend fun ensurePermission(context: Context, device: UsbDevice): Boolean {
        val usb = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (usb.hasPermission(device)) return true
        return withTimeoutOrNull(60_000L) {
            suspendCancellableCoroutine { cont ->
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(c: Context, intent: Intent) {
                        if (intent.action != ACTION_USB_PERMISSION) return
                        try { context.unregisterReceiver(this) } catch (_: Exception) { }
                        if (cont.isActive) cont.resume(usb.hasPermission(device))
                    }
                }
                ContextCompat.registerReceiver(context, receiver, IntentFilter(ACTION_USB_PERMISSION), ContextCompat.RECEIVER_NOT_EXPORTED)
                cont.invokeOnCancellation { try { context.unregisterReceiver(receiver) } catch (_: Exception) { } }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
                val pi = PendingIntent.getBroadcast(
                    context, 0, Intent(ACTION_USB_PERMISSION).setPackage(context.packageName), flags
                )
                usb.requestPermission(device, pi)
            }
        } ?: false
    }

    /** Full mode C readout from the first port of [driver]. */
    suspend fun read(context: Context, driver: UsbSerialDriver, meterNumber: String): MeterResult<MeterReading> {
        val appCtx = context.applicationContext
        if (!ensurePermission(appCtx, driver.device)) return MeterResult.Fail("USB permission was not granted for the probe.")
        return withContext(Dispatchers.IO) {
            val usb = appCtx.getSystemService(Context.USB_SERVICE) as UsbManager
            val connection = usb.openDevice(driver.device) ?: return@withContext MeterResult.Fail("Couldn't open the probe.")
            val port = driver.ports.firstOrNull() ?: return@withContext MeterResult.Fail("The probe has no serial port.")
            try {
                port.open(connection)
                port.setParameters(300, UsbSerialPort.DATABITS_7, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_EVEN)
                try { port.setDTR(true); port.setRTS(true) } catch (_: Exception) { } // some probes are powered by DTR/RTS
                port.write("/?!\r\n".toByteArray(Charsets.US_ASCII), 2000)

                val ident = readLine(port, 3000) ?: return@withContext MeterResult.Fail("No answer from the meter. Check the probe sits on the meter's optical port.")
                if (!ident.startsWith("/") || ident.length < 5) return@withContext MeterResult.Fail("Unexpected answer from the meter: $ident")
                val z = ident[4]
                val modeCBaud = mapOf('0' to 300, '1' to 600, '2' to 1200, '3' to 2400, '4' to 4800, '5' to 9600, '6' to 19200)[z]
                val modeBBaud = mapOf('A' to 600, 'B' to 1200, 'C' to 2400, 'D' to 4800, 'E' to 9600, 'F' to 19200)[z]
                val baud = modeCBaud ?: modeBBaud ?: 300
                if (modeCBaud != null) {
                    // ACK, protocol control '0' (normal), baud Z, mode '0' = data readout
                    port.write(byteArrayOf(ACK, '0'.code.toByte(), z.code.toByte(), '0'.code.toByte(), '\r'.code.toByte(), '\n'.code.toByte()), 2000)
                    Thread.sleep(300)
                }
                if (baud != 300) port.setParameters(baud, UsbSerialPort.DATABITS_7, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_EVEN)

                val block = readDataBlock(port, 20_000) ?: return@withContext MeterResult.Fail("The meter stopped sending before the end of the data block.")
                val lines = block.split("\r\n", "\n").map { it.trim() }.filter { it.isNotEmpty() && it != "!" }
                MeterResult.Ok(ObisParser.toReading(lines, meterNumber, ident.trim()))
            } catch (e: Exception) {
                MeterResult.Fail("Optical readout failed: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                try { port.close() } catch (_: Exception) { }
            }
        }
    }

    private fun readLine(port: UsbSerialPort, timeoutMs: Long): String? {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(64)
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            val n = port.read(buf, 200)
            for (i in 0 until n) {
                val b = (buf[i].toInt() and 0x7F).toByte()
                if (b == '\n'.code.toByte()) return out.toString(Charsets.US_ASCII.name()).trim()
                out.write(b.toInt())
            }
        }
        return null
    }

    private fun readDataBlock(port: UsbSerialPort, timeoutMs: Long): String? {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(256)
        var started = false
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            val n = port.read(buf, 300)
            for (i in 0 until n) {
                val b = (buf[i].toInt() and 0x7F).toByte()
                when {
                    !started && b == STX -> started = true
                    started && b == ETX -> return out.toString(Charsets.US_ASCII.name())
                    started -> out.write(b.toInt())
                }
            }
        }
        return if (started && out.size() > 0) out.toString(Charsets.US_ASCII.name()) else null
    }
}

/** Maps IEC 62056-21 / OBIS data lines to a [MeterReading]. Unknown codes stay in rawLines. */
object ObisParser {
    private val line = Regex("^([0-9A-Za-z.:*&\\-]+)\\(([^)]*)\\)")

    fun toReading(lines: List<String>, meterNumber: String, ident: String): MeterReading {
        var imp: Double? = null
        var export: Double? = null
        var power: Double? = null
        var voltage: Double? = null
        var current: Double? = null
        var pf: Double? = null
        var freq: Double? = null
        var relay = RelayStatus.UNKNOWN
        val flags = mutableSetOf<TamperFlag>()
        var tamperReported = false

        for (l in lines) {
            val m = line.find(l) ?: continue
            val code = m.groupValues[1].substringAfter(':').substringBefore('*').substringBefore('&')
            val rawValue = m.groupValues[2]
            val unit = rawValue.substringAfter('*', "").trim()
            val num = rawValue.substringBefore('*').trim().toDoubleOrNull()
            fun energyKwh(v: Double) = if (unit.equals("Wh", true)) v / 1000 else v
            fun powerW(v: Double) = if (unit.equals("kW", true)) v * 1000 else v
            when (code) {
                "1.8.0", "15.8.0" -> num?.let { imp = energyKwh(it) }
                "2.8.0" -> num?.let { export = energyKwh(it) }
                "1.7.0", "15.7.0", "16.7.0" -> num?.let { power = powerW(it) }
                "32.7.0", "12.7.0" -> num?.let { voltage = it }
                "31.7.0", "11.7.0" -> num?.let { current = it }
                "13.7.0", "33.7.0" -> num?.let { pf = it }
                "14.7.0" -> num?.let { freq = it }
                "96.3.10" -> num?.let { relay = if (it.toInt() == 1) RelayStatus.CONNECTED else RelayStatus.OPEN_OTHER }
                "F.F", "F.F.0" -> {
                    tamperReported = true
                    val bits = rawValue.substringBefore('*').trim().toLongOrNull(16) ?: 0L
                    if (bits != 0L) flags += TamperFlag.METER_FAULT
                }
            }
        }
        if ((export ?: 0.0) > 0.0) flags += TamperFlag.REVERSE_ENERGY
        return MeterReading(
            source = ReadingSource.OPTICAL,
            meterNumber = meterNumber,
            takenAt = System.currentTimeMillis(),
            creditKwh = null, // not a standard IEC 62056-21 register; check the raw lines for your meter's code
            totalImportKwh = imp,
            totalExportKwh = export,
            activePowerW = power,
            voltageV = voltage,
            currentA = current,
            powerFactor = pf,
            frequencyHz = freq,
            relay = relay,
            tamperFlags = flags,
            tamperReported = tamperReported,
            meterIdentification = ident,
            rawLines = lines
        )
    }
}

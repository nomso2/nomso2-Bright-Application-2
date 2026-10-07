package com.example.ui.solutions.common

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.DisCoContact
import com.example.model.DisCoContacts
import com.example.model.PhoneNumbers
import com.example.model.StateDisCoMap
import com.example.model.UserProfile
import java.io.File

/**
 * Hand-offs to the phone's own apps. None of these need a permission: the user sees the
 * dialer / SMS / mail app pre-filled and presses send themselves.
 */
object SolutionIntents {

    private fun launch(context: Context, intent: Intent, failMessage: String): Boolean = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, failMessage, Toast.LENGTH_LONG).show()
        false
    } catch (e: SecurityException) {
        Toast.makeText(context, failMessage, Toast.LENGTH_LONG).show()
        false
    }

    fun dial(context: Context, number: String): Boolean =
        launch(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PhoneNumbers.toDialable(number))), "No phone app found")

    /** USSD codes need '#' encoded, otherwise the dialer drops everything after it. */
    fun dialUssd(context: Context, code: String): Boolean =
        launch(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(code.trim()))), "No phone app found")

    /** Opens the SMS app with recipients and text filled in (several numbers separated by ';'). */
    fun sms(context: Context, recipients: List<String>, body: String): Boolean {
        val to = recipients.joinToString(";") { PhoneNumbers.toDialable(it) }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$to")).putExtra("sms_body", body)
        return launch(context, intent, "No SMS app found")
    }

    fun email(context: Context, to: String, subject: String, body: String): Boolean {
        val uri = Uri.parse("mailto:" + Uri.encode(to) + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(body))
        return launch(context, Intent(Intent.ACTION_SENDTO, uri), "No email app found")
    }

    fun whatsApp(context: Context, number: String, text: String): Boolean {
        val digits = PhoneNumbers.toWhatsAppDigits(number) ?: return false
        val uri = Uri.parse("https://wa.me/$digits?text=" + Uri.encode(text))
        return launch(context, Intent(Intent.ACTION_VIEW, uri), "WhatsApp is not installed")
    }

    fun shareText(context: Context, subject: String, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text)
        return launch(context, Intent.createChooser(send, subject), "No app can share this")
    }

    fun shareFile(context: Context, file: File, mime: String, subject: String): Boolean {
        val uri = fileUri(context, file)
        val send = Intent(Intent.ACTION_SEND).setType(mime)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(send, subject).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return launch(context, chooser, "No app can share this")
    }

    fun openMap(context: Context, lat: Double, lng: Double, label: String): Boolean {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(" + Uri.encode(label) + ")")
        return launch(context, Intent(Intent.ACTION_VIEW, uri), "No maps app found")
    }

    fun openUrl(context: Context, url: String): Boolean =
        launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)), "No browser found")

    fun openAppSettings(context: Context): Boolean = launch(
        context,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName)),
        "Open Settings > Apps > Bright"
    )

    fun copy(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    fun fileUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    /** Best contact for the user's DisCo: profile DisCo code first, then the profile's state. */
    fun discoFor(profile: UserProfile): DisCoContact? =
        DisCoContacts.forCode(profile.discoCode)
            ?: StateDisCoMap.forState(profile.state)?.contacts?.firstOrNull()

    /** NERC complaints desk (NERC Service Charter 2024 and @NERCNG). */
    const val NERC_EMAIL = "complaints@nerc.gov.ng"
    const val NERC_PHONE = "09088999244"
    /** List of NERC Consumer Forum offices, the step after the DisCo's complaints unit. */
    const val NERC_FORUM_OFFICES_URL = "https://nerc.gov.ng/forum-offices/"
    const val EMERGENCY_NUMBER = "112"
}

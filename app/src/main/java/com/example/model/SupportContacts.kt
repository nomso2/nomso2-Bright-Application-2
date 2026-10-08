package com.example.model

/**
 * Bright's own support lines, shown on the Help screen.
 *
 * TODO(support): fill in the real Bright support numbers before release. While a value is empty,
 * the Help screen hides that button. Never put a made-up or placeholder number here: older users
 * will call whatever number they are shown.
 */
object SupportContacts {
    /** Voice line for Bright support, local format (e.g. 080...). Empty = button hidden. */
    const val SUPPORT_PHONE: String = "" // TODO(support): real Bright support phone number

    /** WhatsApp line for Bright support, local format. Empty = button hidden. */
    const val SUPPORT_WHATSAPP: String = "" // TODO(support): real Bright support WhatsApp number

    val hasPhone: Boolean get() = SUPPORT_PHONE.isNotBlank()
    val hasWhatsApp: Boolean get() = SUPPORT_WHATSAPP.isNotBlank()
}

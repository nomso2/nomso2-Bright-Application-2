package com.example.model

/**
 * Real customer-care channels for every Nigerian electricity distributor, and which
 * distributor(s) serve each of the 36 states + FCT.
 *
 * Source: each DisCo's official website, checked Oct 2026 (see notes/disco-numbers-2026-10-07.md
 * in the project workspace for per-number sources). [verifiedOfficial] is false where the
 * official site contradicts itself and the numbers could not be confirmed there.
 *
 * This is deliberately keyed by code string rather than the [DisCo] enum so that
 * contact-only distributors (MainPower for Enugu, Aba Power) can be listed without
 * changing the 11-entry enum that onboarding, rankings and exhaustive `when`s rely on.
 */
data class DisCoContact(
    val code: String,
    val name: String,
    /** Voice lines, primary first, written as on the official site (local format). */
    val phones: List<String>,
    /** WhatsApp lines in local format; use [PhoneNumbers.toWhatsAppDigits] to build wa.me links. */
    val whatsapp: List<String>,
    val email: String,
    /** False when the email address was only found outside the official site. */
    val emailVerified: Boolean,
    val website: String,
    /** False when the numbers could not be confirmed on the DisCo's own site. */
    val verifiedOfficial: Boolean,
    val note: String
) {
    val primaryPhone: String get() = phones.firstOrNull().orEmpty()
}

object DisCoContacts {
    const val SOURCE_FOOTER = "Numbers from official DisCo websites, checked Oct 2026"

    val all: List<DisCoContact> = listOf(
        DisCoContact(
            code = "AEDC",
            name = "Abuja Electricity Distribution Plc",
            phones = listOf("08039070070"),
            whatsapp = listOf("08152141414", "08152151515", "09161012128", "09162012128"),
            email = "customercare@abujaelectricity.com",
            emailVerified = true,
            website = "https://www.abujaelectricity.com",
            verifiedOfficial = true,
            note = "The official contact page lists two different WhatsApp sets; all four are shown."
        ),
        DisCoContact(
            code = "BEDC",
            name = "BEDC Electricity Plc (Benin)",
            phones = listOf("07000002332"),
            whatsapp = listOf("08125277248"),
            email = "customercomplaints@beninelectric.com",
            emailVerified = true,
            website = "https://beninelectric.com",
            verifiedOfficial = true,
            note = "Domain moved to beninelectric.com; old @bedcpower.com addresses are out of date."
        ),
        DisCoContact(
            code = "EKEDC",
            name = "Eko Electricity Distribution Plc",
            phones = listOf("07080671170", "07001235666"),
            whatsapp = emptyList(),
            email = "customercare@ekedp.com",
            emailVerified = true,
            website = "https://www.ekedp.com",
            verifiedOfficial = true,
            note = "24/7 call centre. Each district also has its own customercare email."
        ),
        DisCoContact(
            code = "EEDC",
            name = "Enugu Electricity Distribution Plc",
            phones = listOf("084700100"),
            whatsapp = listOf("08150826060", "08150826061", "08150826233"),
            email = "customerservice@enugudisco.com",
            emailVerified = false,
            website = "https://www.enugudisco.com",
            verifiedOfficial = true,
            note = "WhatsApp 08150826060/61 are WhatsApp/SMS only; 08150826233 is the EEDC self-service bot. Email is from news reports, not the official site."
        ),
        DisCoContact(
            code = "MAINPOWER",
            name = "MainPower Electricity Distribution Ltd (EEDC subsidiary, Enugu State only)",
            phones = listOf("02084700100"),
            whatsapp = emptyList(),
            email = "info@mainpowerdisco.com",
            emailVerified = true,
            website = "https://mainpowerdisco.com",
            verifiedOfficial = true,
            note = "Took over EEDC's Enugu State network in Oct 2024 under an Enugu State (EERC) licence."
        ),
        DisCoContact(
            code = "IBEDC",
            name = "Ibadan Electricity Distribution Plc",
            phones = listOf("07001239999", "09155009999", "08055009999", "09062029009", "09062039009"),
            whatsapp = emptyList(),
            email = "customercare@ibedc.com",
            emailVerified = true,
            website = "https://www.ibedc.com",
            verifiedOfficial = true,
            note = ""
        ),
        DisCoContact(
            code = "IE",
            name = "Ikeja Electric Plc",
            phones = listOf("02017000250", "02012272940"),
            whatsapp = listOf("09088951626"),
            email = "customercare@ikejaelectric.com",
            emailVerified = true,
            website = "https://www.ikejaelectric.com",
            verifiedOfficial = true,
            note = "WhatsApp is the JohnnIE chatbot, which can hand over to a live agent."
        ),
        DisCoContact(
            code = "JED",
            name = "Jos Electricity Distribution Plc",
            phones = listOf("07069403531", "08111793291", "019125187"),
            whatsapp = emptyList(),
            email = "customercare@jedplc.com",
            emailVerified = true,
            website = "https://www.jedplc.com",
            verifiedOfficial = true,
            note = ""
        ),
        DisCoContact(
            code = "KAEDC",
            name = "Kaduna Electricity Distribution Plc (Kaduna Electric)",
            phones = listOf("08174035711", "08189884459", "07002255533"),
            whatsapp = listOf("08189884459"),
            email = "customercare@kadunaelectric.com",
            emailVerified = true,
            website = "https://kadunaelectric.com",
            verifiedOfficial = false,
            note = "The official site is mid-rebuild and shows conflicting numbers. These come from older official pages, the 2024 Q1 report and the official X bio. NERC appointed an interim administrator in Aug 2026, so channels may change."
        ),
        DisCoContact(
            code = "KEDCO",
            name = "Kano Electricity Distribution Plc",
            phones = listOf("070055551111", "08151481786", "08177064982", "08026319997"),
            whatsapp = emptyList(),
            email = "customercare@kedco.ng",
            emailVerified = true,
            website = "https://kedco.ng",
            verifiedOfficial = true,
            note = "Main hotline 0700-5555-1111."
        ),
        DisCoContact(
            code = "PHED",
            name = "Port Harcourt Electricity Distribution Plc",
            phones = listOf("070022557433", "09087838800", "09087838801"),
            whatsapp = listOf("08188206515"),
            email = "info@phed.com.ng",
            emailVerified = true,
            website = "https://phed.com.ng",
            verifiedOfficial = true,
            note = "Main line 0700 2255 7433. WhatsApp from the link on the official site."
        ),
        DisCoContact(
            code = "YEDC",
            name = "Yola Electricity Distribution Company",
            phones = listOf("07000422559", "09038853326", "09038853342"),
            whatsapp = emptyList(),
            email = "info@yedc.com.ng",
            emailVerified = true,
            website = "https://www.yedc.com.ng",
            verifiedOfficial = true,
            note = ""
        ),
        DisCoContact(
            code = "ABAPOWER",
            name = "Aba Power Limited Electric (APLE)",
            phones = listOf("07001238280", "07002338280", "070022276937"),
            whatsapp = listOf("09091866376"),
            email = "customercare@abapower.com",
            emailVerified = true,
            website = "https://www.abapower.com",
            verifiedOfficial = true,
            note = "Serves the Aba ring-fenced area. 070022276937 is copied exactly as shown on the site."
        )
    )

    private val byCode: Map<String, DisCoContact> = all.associateBy { it.code.uppercase() }

    fun forCode(code: String): DisCoContact? = byCode[code.trim().uppercase()]
}

/** One state (or FCT) and the distributor code(s) that serve it, with an area note for split states. */
data class StateDisCo(
    val state: String,
    val discoCodes: List<String>,
    val note: String = ""
) {
    val contacts: List<DisCoContact> get() = discoCodes.mapNotNull { DisCoContacts.forCode(it) }
    val isSplit: Boolean get() = discoCodes.size > 1
}

object StateDisCoMap {
    /** All 36 states + FCT, alphabetical (FCT sorted under F). */
    val states: List<StateDisCo> = listOf(
        StateDisCo("Abia", listOf("EEDC", "ABAPOWER"), "Aba Power serves the Aba ring-fenced area; EEDC serves the rest."),
        StateDisCo("Adamawa", listOf("YEDC"), ""),
        StateDisCo("Akwa Ibom", listOf("PHED"), ""),
        StateDisCo("Anambra", listOf("EEDC"), ""),
        StateDisCo("Bauchi", listOf("JED"), ""),
        StateDisCo("Bayelsa", listOf("PHED"), ""),
        StateDisCo("Benue", listOf("JED"), ""),
        StateDisCo("Borno", listOf("YEDC"), ""),
        StateDisCo("Cross River", listOf("PHED"), ""),
        StateDisCo("Delta", listOf("BEDC"), ""),
        StateDisCo("Ebonyi", listOf("EEDC"), ""),
        StateDisCo("Edo", listOf("BEDC"), ""),
        StateDisCo("Ekiti", listOf("BEDC", "IBEDC"), "BEDC is the main DisCo; IBEDC says it serves 'parts of Ekiti'."),
        StateDisCo("Enugu", listOf("MAINPOWER"), "MainPower (an EEDC subsidiary) took over from EEDC in Oct 2024 under an EERC licence."),
        StateDisCo("FCT", listOf("AEDC"), ""),
        StateDisCo("Gombe", listOf("JED"), ""),
        StateDisCo("Imo", listOf("EEDC"), ""),
        StateDisCo("Jigawa", listOf("KEDCO"), ""),
        StateDisCo("Kaduna", listOf("KAEDC"), ""),
        StateDisCo("Kano", listOf("KEDCO"), ""),
        StateDisCo("Katsina", listOf("KEDCO"), ""),
        StateDisCo("Kebbi", listOf("KAEDC"), ""),
        StateDisCo("Kogi", listOf("AEDC", "IBEDC"), "AEDC is the main DisCo (Lokoja, Okene, Kabba, Idah, Anyigba area offices); IBEDC says it serves 'parts of Kogi'."),
        StateDisCo("Kwara", listOf("IBEDC"), ""),
        StateDisCo("Lagos", listOf("EKEDC", "IE"), "Eko serves Lagos South/Island; Ikeja serves Lagos North/mainland."),
        StateDisCo("Nasarawa", listOf("AEDC"), ""),
        StateDisCo("Niger", listOf("AEDC", "IBEDC"), "AEDC serves Minna, Bida, Suleja, Kontagora and Madalla; IBEDC says it serves 'parts of Niger'."),
        StateDisCo("Ogun", listOf("IBEDC", "EKEDC", "IE"), "IBEDC serves most of the state. EKEDC serves Agbara. IE's Abule-Egba business unit serves Ogun-border feeders such as Akute, Ijoko, Agbado and Ota-Amje (per NERC energy-cap filings)."),
        StateDisCo("Ondo", listOf("BEDC"), ""),
        StateDisCo("Osun", listOf("IBEDC"), ""),
        StateDisCo("Oyo", listOf("IBEDC"), ""),
        StateDisCo("Plateau", listOf("JED"), ""),
        StateDisCo("Rivers", listOf("PHED"), ""),
        StateDisCo("Sokoto", listOf("KAEDC"), ""),
        StateDisCo("Taraba", listOf("YEDC"), ""),
        StateDisCo("Yobe", listOf("YEDC"), ""),
        StateDisCo("Zamfara", listOf("KAEDC"), "")
    )

    val stateNames: List<String> = states.map { it.state }

    /**
     * Matches free-text profile values such as "Lagos State", "lagos", "Abuja",
     * "Federal Capital Territory" or "Akwa-Ibom" to a canonical state row.
     */
    fun forState(raw: String?): StateDisCo? {
        val key = normalise(raw ?: return null)
        if (key.isEmpty()) return null
        return states.firstOrNull { normalise(it.state) == key }
    }

    /** First state a distributor serves, used when the profile has no usable state. */
    fun firstStateServedBy(discoCode: String): StateDisCo? =
        states.firstOrNull { row -> row.discoCodes.any { it.equals(discoCode.trim(), ignoreCase = true) } }

    fun search(query: String): List<StateDisCo> {
        val q = query.trim()
        if (q.isEmpty()) return states
        return states.filter { it.state.contains(q, ignoreCase = true) || normalise(it.state).startsWith(normalise(q)) }
    }

    private fun normalise(raw: String): String {
        var s = raw.lowercase().replace("-", " ").replace(Regex("\\s+"), " ").trim()
        s = s.removeSuffix(" state").trim()
        if (s == "abuja" || s == "federal capital territory" || s == "fct abuja" || s == "abuja fct") s = "fct"
        return s.replace(" ", "")
    }
}

/** Helpers for turning the official local-format numbers into dial / WhatsApp targets. */
object PhoneNumbers {
    /** Digits only (keeps a leading +) for a tel: URI. */
    fun toDialable(raw: String): String {
        val trimmed = raw.trim()
        val digits = trimmed.filter { it.isDigit() }
        return if (trimmed.startsWith("+")) "+$digits" else digits
    }

    /**
     * International digits for https://wa.me/ (no +, no leading 0): 08152141414 -> 2348152141414.
     * Returns null when the number cannot be normalised.
     */
    fun toWhatsAppDigits(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        val intl = when {
            digits.startsWith("234") -> digits
            digits.startsWith("0") && digits.length > 1 -> "234" + digits.drop(1)
            digits.length == 10 -> "234$digits"
            else -> return null
        }
        return if (intl.length in 12..14) intl else null
    }
}

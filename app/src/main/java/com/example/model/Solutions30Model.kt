package com.example.model

/**
 * 30 Problems & Solutions in the Nigerian Power Sector
 */
enum class SolutionCategory(val id: Int, val title: String, val iconName: String) {
    OUTAGE_CATEGORISATION(1, "Outage Categorisation & Smart Dispatching", "flash_on"),
    NETWORK_HARDWARE_FAILURES(2, "Network & Hardware Failures", "signal_cellular_alt"),
    BILLING_TARIFFS_FINANCIAL(3, "Billing, Tariffs & Accountability", "account_balance_wallet"),
    NEIGHBOR_BOTTLENECK_CROWDSOURCING(4, "Neighbor Bottleneck & Crowdsourcing", "people"),
    MAINTENANCE_TRANSPARENCY(5, "Maintenance Transparency & Oversight", "build"),
    PERSONAL_ENERGY_MANAGEMENT(6, "Personal Energy & Surge Protection", "battery_charging_full")
}

data class PowerProblemSolution(
    val problemNumber: Int,
    val category: SolutionCategory,
    val problemStatement: String,
    val solutionTitle: String,
    val solutionDetail: String,
    val regulatoryBadge: String? = null
)

object PowerSector30Registry {
    val ALL_30_SOLUTIONS = listOf(
        // CATEGORY 1: Outage Categorisation & Smart Dispatching
        PowerProblemSolution(
            problemNumber = 1,
            category = SolutionCategory.OUTAGE_CATEGORISATION,
            problemStatement = "DisCos are overwhelmed by thousands of raw, unorganized outage alerts at once.",
            solutionTitle = "Tiered Urgency Categoriser",
            solutionDetail = "Groups alerts: Tier 1 (single house / breaker trip), Tier 2 (street / fused transformer), Tier 3 (district / 33kV injection substation collapse).",
            regulatoryBadge = "NERC Grid Code 2023"
        ),
        PowerProblemSolution(
            problemNumber = 2,
            category = SolutionCategory.OUTAGE_CATEGORISATION,
            problemStatement = "Physical maintenance crews get lost or go to the wrong locations due to poor mapping.",
            solutionTitle = "GPS Geofencing for Faults",
            solutionDetail = "Clusters user reports on an interactive map, dropping a pinpoint exactly where the fault originates so field teams can drive straight to it.",
            regulatoryBadge = "GIS GIS-TCN Telemetry"
        ),
        PowerProblemSolution(
            problemNumber = 3,
            category = SolutionCategory.OUTAGE_CATEGORISATION,
            problemStatement = "DisCos manually route technicians, which takes days.",
            solutionTitle = "Automated Dispatch Router (Uber for Electricians)",
            solutionDetail = "Pushes high-priority neighborhood faults directly to the closest registered DisCo field team with technician name, vehicle reg, live ETA, and direct call link.",
            regulatoryBadge = "DisCo SLA Level 1"
        ),
        PowerProblemSolution(
            problemNumber = 4,
            category = SolutionCategory.OUTAGE_CATEGORISATION,
            problemStatement = "Urgent public safety hazards (like a fallen high-tension wire) are treated with the same priority as a regular blackout.",
            solutionTitle = "Critical Danger Red Button",
            solutionDetail = "Bypasses regular queues; instant 1-tap SOS with AI photo verification immediately alerting DisCo emergency control room to isolate the line.",
            regulatoryBadge = "Zero Harm Protocol"
        ),
        PowerProblemSolution(
            problemNumber = 5,
            category = SolutionCategory.OUTAGE_CATEGORISATION,
            problemStatement = "Customers don't know why the lights went out (Load shedding vs. Actual fault).",
            solutionTitle = "Diagnostic Status Tracker",
            solutionDetail = "Clearly informs users: 'Your light was intentionally cut for load-shedding (TCN quota), expect it back at 4:00 PM' vs 'An unplanned fault occurred.'",
            regulatoryBadge = "MYTO Transparency"
        ),

        // CATEGORY 2: Network & Hardware Failures
        PowerProblemSolution(
            problemNumber = 6,
            category = SolutionCategory.NETWORK_HARDWARE_FAILURES,
            problemStatement = "The internet or mobile data network drops during a massive blackout.",
            solutionTitle = "Offline USSD / SMS Data Bridge",
            solutionDetail = "Allows users to log complaints via text message structures (*384*55# or SMS), which the system automatically translates into backend tickets.",
            regulatoryBadge = "NCC Offline Bridge"
        ),
        PowerProblemSolution(
            problemNumber = 7,
            category = SolutionCategory.NETWORK_HARDWARE_FAILURES,
            problemStatement = "Smart meters get physically vandalized or bypassed by thieves.",
            solutionTitle = "Tamper Crowdsourcing",
            solutionDetail = "If a street's smart meter suddenly stops sending telemetry while local residents remain active on mobile, the app automatically flags a 'Potential Vandalism / Cable Theft Event'.",
            regulatoryBadge = "Anti-Theft Protocol"
        ),
        PowerProblemSolution(
            problemNumber = 8,
            category = SolutionCategory.NETWORK_HARDWARE_FAILURES,
            problemStatement = "Phone batteries die completely during a multi-day blackout.",
            solutionTitle = "Low-Power 'Bat-Signal' Mode",
            solutionDetail = "A bare-minimum, high-contrast text-only monochrome interface inside the app that uses <1% battery to send an emergency fault ping before the phone dies.",
            regulatoryBadge = "OLED Ultra-Saver"
        ),
        PowerProblemSolution(
            problemNumber = 9,
            category = SolutionCategory.NETWORK_HARDWARE_FAILURES,
            problemStatement = "Smart meters fail to sync up with the user's billing account.",
            solutionTitle = "Universal Meter Sync (Barcode & QR)",
            solutionDetail = "Allows users to scan the barcode or QR code on any prepaid meter to lock in their meter number, house address, feeder band, and DisCo automatically.",
            regulatoryBadge = "MAP Standard"
        ),
        PowerProblemSolution(
            problemNumber = 10,
            category = SolutionCategory.NETWORK_HARDWARE_FAILURES,
            problemStatement = "The national grid collapses completely, leaving DisCos in the dark.",
            solutionTitle = "National Grid Pulse Monitor",
            solutionDetail = "Aggregates transmission company data (TCN NCC Osogbo) and gives users a real-time health bar of the national grid frequency (50Hz) and generation MW.",
            regulatoryBadge = "TCN NCC Telemetry"
        ),

        // CATEGORY 3: Billing, Tariffs, & Financial Accountability
        PowerProblemSolution(
            problemNumber = 11,
            category = SolutionCategory.BILLING_TARIFFS_FINANCIAL,
            problemStatement = "DisCos cheat consumers by placing them on Band A tariffs but giving them Band C hours.",
            solutionTitle = "Automated Band Auditor",
            solutionDetail = "Tracks exactly how many hours of power the user receives each month and generates a downloadable NERC evidence report proving under-delivery.",
            regulatoryBadge = "NERC Order 2024/002"
        ),
        PowerProblemSolution(
            problemNumber = 12,
            category = SolutionCategory.BILLING_TARIFFS_FINANCIAL,
            problemStatement = "Getting a rebate or credit for prolonged blackouts is a bureaucratic nightmare.",
            solutionTitle = "Automated Refund Ledger",
            solutionDetail = "Logs uninterrupted hours of darkness and auto-fills a formal NERC refund claim form when the DisCo violates service level agreements.",
            regulatoryBadge = "NERC CPR 2023"
        ),
        PowerProblemSolution(
            problemNumber = 13,
            category = SolutionCategory.BILLING_TARIFFS_FINANCIAL,
            problemStatement = "Consumers get hit with arbitrary 'Estimated Bills' because they don't have a meter.",
            solutionTitle = "Community Consumption Calculator",
            solutionDetail = "Averages the verified prepaid usage of neighbors on the same street to scientifically prove what an unmetered house actually owes.",
            regulatoryBadge = "Capped Billing Order"
        ),
        PowerProblemSolution(
            problemNumber = 14,
            category = SolutionCategory.BILLING_TARIFFS_FINANCIAL,
            problemStatement = "DisCos hoard physical meters in warehouses instead of installing them.",
            solutionTitle = "Meter Waitlist Tracker & Hall of Shame",
            solutionDetail = "Users log the day they paid for a meter; the app publicly ranks DisCos by installation delay days past the NERC 10-day limit to demand accountability.",
            regulatoryBadge = "NERC MAP Regulations"
        ),
        PowerProblemSolution(
            problemNumber = 15,
            category = SolutionCategory.BILLING_TARIFFS_FINANCIAL,
            problemStatement = "Buying token units when the power is out often fails due to bad network gateways.",
            solutionTitle = "Offline Token Vending via SMS",
            solutionDetail = "Queues token purchases over secure SMS/USSD channels so users can purchase STS prepaid credit without active internet access.",
            regulatoryBadge = "STS 20-Digit Standard"
        ),

        // CATEGORY 4: The "Neighbor Bottleneck" & Crowdsourcing
        PowerProblemSolution(
            problemNumber = 16,
            category = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING,
            problemStatement = "NERC's app requires 3 neighbors to verify a fault, which leaves isolated users stranded.",
            solutionTitle = "Visual Proof Overrides",
            solutionDetail = "Allows a single user to bypass the 3-neighbor rule by uploading a time-stamped, geotagged photo or video of the specific electrical fault.",
            regulatoryBadge = "Fast-Track Protocol"
        ),
        PowerProblemSolution(
            problemNumber = 17,
            category = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING,
            problemStatement = "Most neighbors don't know the app exists, so nobody reports faults.",
            solutionTitle = "'Wake Up the Street' Alerts",
            solutionDetail = "Allows one user to trigger a free localized SMS broadcast to invite nearby neighbors on the same transformer to open the app and co-sign the report.",
            regulatoryBadge = "Cluster Telemetry"
        ),
        PowerProblemSolution(
            problemNumber = 18,
            category = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING,
            problemStatement = "Malicious users log fake outages to mess with the system.",
            solutionTitle = "User Trust Score",
            solutionDetail = "Every user starts with a neutral score; successful, verified reports increase their trust rating (+5 pts), while false alarms reduce it (-15 pts).",
            regulatoryBadge = "Reputation Engine"
        ),
        PowerProblemSolution(
            problemNumber = 19,
            category = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING,
            problemStatement = "Residents don't know who their local community electrical representative or 'chairman' is.",
            solutionTitle = "Neighborhood Grid Forum",
            solutionDetail = "A built-in localized chat board exclusive to every transformer zone where residents discuss local electrical issues and anti-extortion alerts safely.",
            regulatoryBadge = "Community Vigilance"
        ),
        PowerProblemSolution(
            problemNumber = 20,
            category = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING,
            problemStatement = "Language barriers prevent less-educated citizens from reporting issues.",
            solutionTitle = "Multi-Lingual Audio Voice Reporting",
            solutionDetail = "Supports Pidgin ('Light don quench'), Hausa, Yoruba, and Igbo voice notes using AI speech conversion to produce standard technical tickets.",
            regulatoryBadge = "Universal Access"
        ),

        // CATEGORY 5: Maintenance Transparency & Accountability
        PowerProblemSolution(
            problemNumber = 21,
            category = SolutionCategory.MAINTENANCE_TRANSPARENCY,
            problemStatement = "DisCo maintenance teams demand bribes ('money for fuel or cables') before fixing a fault.",
            solutionTitle = "Anonymous Whistleblower Tool",
            solutionDetail = "Users can securely report extortion attempts, automatically forwarding the technician's logged ID and demand straight to NERC compliance.",
            regulatoryBadge = "Anti-Extortion Law"
        ),
        PowerProblemSolution(
            problemNumber = 22,
            category = SolutionCategory.MAINTENANCE_TRANSPARENCY,
            problemStatement = "Once a report is submitted, it falls into a 'black hole' with zero updates.",
            solutionTitle = "Pizza-Style Delivery Tracker",
            solutionDetail = "Shows 5 exact progress stages: Ticket Created → DisCo Acknowledged → Crew Dispatched → Repair in Progress → Restored & Verified.",
            regulatoryBadge = "Live Progress Bar"
        ),
        PowerProblemSolution(
            problemNumber = 23,
            category = SolutionCategory.MAINTENANCE_TRANSPARENCY,
            problemStatement = "DisCo technicians mark a ticket as 'Fixed' on their end when the street is still dark.",
            solutionTitle = "Consumer Closure Verification",
            solutionDetail = "A ticket cannot close until a random sample of users in that geofence tap 'Yes, my light is back' inside the app.",
            regulatoryBadge = "Consumer Gatekeeper"
        ),
        PowerProblemSolution(
            problemNumber = 24,
            category = SolutionCategory.MAINTENANCE_TRANSPARENCY,
            problemStatement = "Substandard, cheap parts are used by technicians, causing the transformer to blow again two days later.",
            solutionTitle = "Fault History Log",
            solutionDetail = "If a transformer breaks down more than twice in a month, the app flags it as a 'Recurrent Infrastructure Failure' for premium NERC engineering overhaul.",
            regulatoryBadge = "Asset Lifecycle Audit"
        ),
        PowerProblemSolution(
            problemNumber = 25,
            category = SolutionCategory.MAINTENANCE_TRANSPARENCY,
            problemStatement = "DisCos claim they don't have the materials (cables, fuses) in stock to fix the issue.",
            solutionTitle = "Inventory Request Monitor",
            solutionDetail = "Makes the DisCo state exactly what part is missing, allowing the community to track if the delay is logistical or intentional stalling for bribes.",
            regulatoryBadge = "Supply Chain Audit"
        ),

        // CATEGORY 6: Personal Energy Management & Efficiency
        PowerProblemSolution(
            problemNumber = 26,
            category = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT,
            problemStatement = "High voltage surges destroy expensive home appliances when power returns.",
            solutionTitle = "Surge Return Warning System",
            solutionDetail = "Sends a high-priority push notification 5 minutes before scheduled line energization, advising users to unplug delicate electronic devices.",
            regulatoryBadge = "Pre-Restoration Surge Alert"
        ),
        PowerProblemSolution(
            problemNumber = 27,
            category = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT,
            problemStatement = "Consumers burn expensive generator fuel because they don't know the grid light just came back on.",
            solutionTitle = "'Grid is Back' Audio Siren",
            solutionDetail = "The moment a neighbor reports light or a smart meter registers current, sounds a loud, distinct alarm to notify you to switch off your generator.",
            regulatoryBadge = "Fuel Saver Protocol"
        ),
        PowerProblemSolution(
            problemNumber = 28,
            category = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT,
            problemStatement = "Users don't know how to optimize their homes to stay within a cheaper tariff band.",
            solutionTitle = "Appliance Load Budgeter",
            solutionDetail = "Users select what electronics they own, and the app calculates how to run them without triggering peak surcharges or overloading inverters.",
            regulatoryBadge = "Demand Side Management"
        ),
        PowerProblemSolution(
            problemNumber = 29,
            category = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT,
            problemStatement = "Solar and inverter users don't know when to rely on the sun vs. when to use the grid.",
            solutionTitle = "Hybrid Energy Optimizer",
            solutionDetail = "Connects weather data with grid outage patterns to advise: 'Charge your batteries now, a grid outage is highly likely in 2 hours.'",
            regulatoryBadge = "Solar-Grid AI Sync"
        ),
        PowerProblemSolution(
            problemNumber = 30,
            category = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT,
            problemStatement = "The public is completely unaware of changing energy laws and tariff hikes until they happen.",
            solutionTitle = "Tariff Flash News Feed",
            solutionDetail = "A simple, jargon-free news section that breaks down complex regulatory changes into clear statements on how much it will cost the average citizen.",
            regulatoryBadge = "Consumer Advisory"
        )
    )
}

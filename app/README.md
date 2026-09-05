# Bright: Fixing Nigeria's Electricity Response Crisis

**Bright** is an AI-assisted, mission-critical citizen utility & smart grid accountability Android application built with Kotlin and Jetpack Compose. It bridges the transparency gap between Nigerian electricity consumers, the 11 Distribution Companies (DisCos), Transmission Company of Nigeria (TCN), and the Nigerian Electricity Regulatory Commission (NERC).

---

## ⚡ Key Capabilities & Architecture

### 1. Comprehensive Fault Reporting with Visual Evidence
- **Photo & Short Video Uploads**: Built with Android's zero-permission `PickVisualMedia` Photo Picker. Allows consumers and community leaders to attach evidence of blown transformers, snapped concrete poles, sparking feeder lines, or illegal meter bypasses.
- **Auto-Clustering Engine**: Groups multiple reports from neighbors on the same distribution transformer (`TR-VI-ADEOLA-04B`) into a single high-priority outage incident, preventing ticket duplication and enabling faster DisCo crew dispatch.
- **1-Tap Emergency SOS Fast-Track**: High-voltage hazard escalation for life-threatening emergencies (live wires touching floodwater, sparking transformers).

### 2. Detailed Historical Fault Audit Log ('History' Tab)
- **Comprehensive History Screen**: Provides an auditable lifecycle log of all lodged faults and billing disputes.
- **Key Performance Indicators (KPIs)**: Displays Total Logged, Confirmed Resolved, and Average Turnaround Time.
- **Filterable Statuses**: Filter by *All*, *Resolved*, *In-Progress*, and *NERC Disputes*.
- **Detailed History Entries**: Nature of complaint, date lodged, current status, resolution timestamp, assigned technical crew details, and full-screen media inspection.

### 3. Information Hub (Tariffs, Billing & NERC Policies)
- **Approved Multi-Year Tariff Schedule (MYTO 2026)**: Detailed rates for Bands A (₦206.80/kWh), B (₦63.00/kWh), C (₦50.00/kWh), D (₦33.00/kWh), and E (₦32.00/kWh).
- **Interactive Tariff & Token Unit Calculator**: Calculates net energy units (kWh) receivable for any recharge amount after statutory 7.5% VAT.
- **Billing Methodologies (Prepaid vs. Estimated)**:
  - *Prepaid STS Class*: Token generation, CIU keypad entry, and real-time load monitoring.
  - *Estimated Billing & Capping Order (NERC/REG/2020/004)*: Strict legal energy capping rules and pro-rata outage reductions.
  - *MD vs. Non-MD*: Commercial demand charges vs. residential flat rates.
- **NERC Consumer Protection Regulations (CPR 2023)**:
  - 48-Hour Written Notice before disconnection.
  - Prohibition of Disconnection during active billing disputes.
  - Prohibition of consumer infrastructure co-funding without explicit meter credit agreements.
  - 15-day maximum complaint resolution guarantee.
- **All 11 DisCos Directory & Scorecards**: Coverage, headquarters, customer care helplines, and NERC performance ratings for AEDC, BEDC, EKEDC, EEDC, IBEDC, IE, JED, KAEDC, KEDCO, PHED, and YEDC.

### 4. Real-time Google Maps & Routing Agent
- **Places & Infrastructure Search**: Find nearest DisCo customer service centers, 33kV/11kV substations, emergency transformer workshops, and NERC Forum offices.
- **Live Route & Turn-by-Turn Navigation**: Computes route distance, travel duration in traffic, turn-by-turn maneuvers, and deep-links directly into official Google Maps navigation.

### 5. SCADA Grid Telemetry & Anti-Vandalism Whistleblowing
- **Interactive SCADA Outage Map Canvas**: Visualized feeder nodes, real-time trip states, and maintenance windows.
- **Confidential Vandalism Reporting**: Anonymous reporting for transformer oil siphoning, aluminum conductor theft, and armored cable vandalism.

---

## 🛠 Tech Stack
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Theme**: "Elegant Dark" Palette (`#0A0C10` Void Black, `#12161F` Surface, `#F59E0B` High-Voltage Gold, `#10B981` Restored Emerald, `#EF4444` Hazard Crimson)
- **Local Persistence**: Room Database (`androidx.room`) with Coroutines & StateFlow
- **Media**: Zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`)
- **Maps**: Google Maps Intent Engine & Spatial Intelligence Service
- **Testing**: Robolectric & Roborazzi screenshot verification

---

## 🚀 Building the Project
```bash
# Clone the repository
git clone <repository_url>

# Open in Android Studio (Ladybug / Koala or newer)
# Build debug APK
gradle assembleDebug

# Run Robolectric Unit Tests
gradle :app:testDebugUnitTest
```

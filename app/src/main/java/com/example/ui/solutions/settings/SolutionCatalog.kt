package com.example.ui.solutions.settings

import com.example.model.SolutionCategory
import com.example.ui.solutions.common.BrightPermissions

/** Plain-language names and one-line explanations for all 30 solutions (no jargon in the UI). */
data class SolutionInfo(
    val number: Int,
    val category: SolutionCategory,
    val title: String,
    val summary: String,
    /** Runtime permissions the feature needs to work in the background (empty = none). */
    val permissions: List<String> = emptyList()
)

object SolutionCatalog {
    /** The five that stay as on-screen buttons. */
    val QUICK_ACTIONS = listOf(4, 20, 16, 2, 6)

    private val C1 = SolutionCategory.OUTAGE_CATEGORISATION
    private val C2 = SolutionCategory.NETWORK_HARDWARE_FAILURES
    private val C3 = SolutionCategory.BILLING_TARIFFS_FINANCIAL
    private val C4 = SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING
    private val C5 = SolutionCategory.MAINTENANCE_TRANSPARENCY
    private val C6 = SolutionCategory.PERSONAL_ENERGY_MANAGEMENT

    val all: List<SolutionInfo> = listOf(
        SolutionInfo(1, C1, "How serious is the outage", "Sorts your report: just your house, your street, or the whole area."),
        SolutionInfo(2, C1, "Pin where the fault is", "Uses your location so the repair team finds the spot.", BrightPermissions.LOCATION),
        SolutionInfo(3, C1, "Nearest repair team", "Shows which team is closest and how long they may take."),
        SolutionInfo(4, C1, "Danger button", "For fallen or sparking wires. Calls for help fast."),
        SolutionInfo(5, C1, "Why is my light off?", "Tells you if it is a planned cut or a fault."),
        SolutionInfo(6, C2, "Report by text message", "Report with SMS when there is no internet."),
        SolutionInfo(7, C2, "Stolen cables or broken meter", "Report theft or damage with a photo."),
        SolutionInfo(8, C2, "Low battery mode", "A simple black screen to send an alert when your battery is low."),
        SolutionInfo(9, C2, "My meter", "Scan or type your meter number."),
        SolutionInfo(10, C2, "National power status", "Shows if the national supply is steady."),
        SolutionInfo(11, C3, "Hours of light check", "Counts your daily hours of light against what you pay for.", BrightPermissions.NOTIFICATIONS),
        SolutionInfo(12, C3, "Track when light goes and comes back", "Works out money owed to you when you get fewer hours.", BrightPermissions.NOTIFICATIONS),
        SolutionInfo(13, C3, "Fair bill check", "Compare an estimated bill with what neighbours really use."),
        SolutionInfo(14, C3, "Waiting for a meter", "Counts the days since you paid for a meter."),
        SolutionInfo(15, C3, "Buy units without internet", "Use your bank's * code to buy units."),
        SolutionInfo(16, C4, "Send a photo of the fault", "One clear photo is enough to report a fault."),
        SolutionInfo(17, C4, "Ask neighbours to report too", "After you report, Bright offers to invite neighbours."),
        SolutionInfo(18, C4, "Your reporting score", "Goes up when your reports are confirmed."),
        SolutionInfo(19, C4, "Neighbours' notice board", "Messages for everyone on your transformer."),
        SolutionInfo(20, C4, "Speak your report", "Hold the button and talk, in your own language."),
        SolutionInfo(21, C5, "Report a bribe request", "Tell NERC privately if someone asks you for money."),
        SolutionInfo(22, C5, "Follow your report", "Shows each step from report to repair.", BrightPermissions.NOTIFICATIONS),
        SolutionInfo(23, C5, "Confirm the light is back", "A repair only counts when you say yes."),
        SolutionInfo(24, C5, "Repeat faults", "Warns when your transformer keeps breaking."),
        SolutionInfo(25, C5, "Missing parts check", "Keeps track when you are told a part is not in stock."),
        SolutionInfo(26, C6, "Unplug reminder", "Reminds you to unplug appliances when power returns.", BrightPermissions.NOTIFICATIONS),
        SolutionInfo(27, C6, "Light is back alert", "A gentle sound when power returns, so you can switch off the generator.", BrightPermissions.NOTIFICATIONS),
        SolutionInfo(28, C6, "Appliance cost planner", "See what your appliances cost each month."),
        SolutionInfo(29, C6, "Solar and battery advice", "When to charge from the grid and when to use the sun.", BrightPermissions.LOCATION),
        SolutionInfo(30, C6, "Electricity price news", "Price changes explained simply.", BrightPermissions.NOTIFICATIONS)
    )

    fun info(number: Int): SolutionInfo = all.first { it.number == number }

    /** The 25 that live in Settings. */
    val settingsBased: List<SolutionInfo> get() = all.filter { it.number !in QUICK_ACTIONS }

    /** Plain names for the six groups. */
    fun categoryTitle(c: SolutionCategory): String = when (c) {
        SolutionCategory.OUTAGE_CATEGORISATION -> "Reporting a fault"
        SolutionCategory.NETWORK_HARDWARE_FAILURES -> "No network, theft and meters"
        SolutionCategory.BILLING_TARIFFS_FINANCIAL -> "Bills and money owed"
        SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING -> "Neighbours"
        SolutionCategory.MAINTENANCE_TRANSPARENCY -> "Repairs"
        SolutionCategory.PERSONAL_ENERGY_MANAGEMENT -> "Your home and appliances"
    }
}

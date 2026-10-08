package com.example.ui.theme

/** How the app picks light or dark colours. SYSTEM follows the phone's own setting. */
enum class ThemeMode(val label: String) {
    SYSTEM("Same as my phone"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

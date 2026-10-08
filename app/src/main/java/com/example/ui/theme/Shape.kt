package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** One soft corner everywhere: 16dp. Calm, friendly, and easy to see as "a button". */
val BrightCornerShape = RoundedCornerShape(16.dp)

/** Shape for rectangular buttons in shared components (instead of M3's default pill). */
val BrightButtonShape = BrightCornerShape

val BrightShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = BrightCornerShape,
    medium = BrightCornerShape,
    large = BrightCornerShape,
    extraLarge = RoundedCornerShape(24.dp)
)

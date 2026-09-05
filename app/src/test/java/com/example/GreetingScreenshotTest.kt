package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.components.MeterProfileHeader
import com.example.ui.theme.BrightTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun bright_meter_profile_screenshot() {
        val testProfile = UserProfile(
            meterNumber = "01429583192",
            customerName = "Chuka Obunma",
            phoneNumber = "+234 803 892 4110",
            streetAddress = "14 Adeola Odeku Street, Victoria Island",
            discoCode = "EKEDC",
            feederBand = FeederBand.BAND_A,
            transformerId = "TR-VI-ADEOLA-04B"
        )

        composeTestRule.setContent {
            BrightTheme {
                MeterProfileHeader(
                    profile = testProfile,
                    onEditProfileClicked = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/meter_profile.png")
    }
}

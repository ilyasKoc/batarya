package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.system.LiveBatteryInfo
import com.example.ui.screens.BatteryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PowerMode
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun battery_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BatteryScreen(
          batteryInfo = LiveBatteryInfo(
            percentage = 84,
            isCharging = true,
            status = "Şarj Ediliyor ⚡",
            plugType = "Hızlı AC Adaptör",
            health = "Mükemmel (İyi)",
            temperatureCelsius = 32.4f,
            voltageMv = 4150,
            technology = "Li-ion",
            estimatedHoursRemaining = 1.2f,
            screenOnEstimatedHours = 5.8f,
            dailyDrainPercent = 26
          ),
          snapshots = emptyList(),
          powerMode = PowerMode.NORMAL,
          onPowerModeChange = {},
          fullChargeAlarmEnabled = true,
          fullChargeTargetPercent = 90,
          onToggleAlarm = {},
          onSetAlarmTarget = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

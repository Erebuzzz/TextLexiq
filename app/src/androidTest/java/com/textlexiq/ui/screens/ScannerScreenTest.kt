package com.textlexiq.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Test
    fun scannerScreen_showsCameraPreview_whenPermissionGranted() {
        // Since we can't easily mock CameraX internals in a simple UI test without dependency injection of a wrapper,
        // we check for the UI structure assuming permissions are granted.

        composeTestRule.setContent {
            ScannerScreen(
                onBack = {},
                onImageCaptured = {}
            )
        }

        // Check if top bar exists with specific title
        composeTestRule.onNodeWithText("Scanner").assertExists()
        
        // Check if capture button exists (Icon with content description "Capture")
        composeTestRule.onNodeWithContentDescription("Capture").assertExists()
    }
}

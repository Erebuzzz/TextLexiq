package com.textlexiq.ui.navigation

import android.net.Uri

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Scanner : Screen("scanner", "Scanner")

    object Crop : Screen("crop", "Crop & Preview") {
        const val imagePathArg = "imagePath"

        fun routeWithArgs(): String = "$route?${imagePathArg}={${imagePathArg}}"

        fun createRoute(imagePath: String): String =
            "$route?${imagePathArg}=${Uri.encode(imagePath)}"
    }

    object Ocr : Screen("ocr", "OCR") {
        const val imagePathArg = "imagePath"

        fun routeWithArgs(): String = "$route?${imagePathArg}={${imagePathArg}}"

        fun createRoute(imagePath: String): String =
            "$route?${imagePathArg}=${Uri.encode(imagePath)}"
    }

    object Document : Screen("document", "Documents") {
        const val documentIdArg = "documentId"

        fun routeWithArgs(): String = "$route?${documentIdArg}={${documentIdArg}}"

        fun createRoute(documentId: Long): String =
            "$route?${documentIdArg}=$documentId"
    }
    object Settings : Screen("settings", "Settings")
    object ModelManagement : Screen("model_management", "AI Models")
    object Onboarding : Screen("onboarding", "Welcome")
}

package com.textlexiq.ui.navigation

import android.net.Uri

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Scanner : Screen("scanner", "Scanner")

    object Crop : Screen("crop/{imagePath}?corners={corners}", "Adjust") {
        const val imagePathArg = "imagePath"
        const val cornersArg = "corners"

        fun routeWithArgs(): String = "crop/{$imagePathArg}?corners={$cornersArg}"

        fun createRoute(path: String, corners: String? = null): String {
             val cornersParam = corners ?: ""
             return "crop/${Uri.encode(path)}?corners=${Uri.encode(cornersParam)}"
        }
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

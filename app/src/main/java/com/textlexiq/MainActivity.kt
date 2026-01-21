package com.textlexiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.textlexiq.ui.navigation.Screen
import com.textlexiq.ui.screens.CropAndPreviewScreen
import com.textlexiq.ui.screens.DocumentViewScreen
import com.textlexiq.ui.screens.HomeScreen
import com.textlexiq.ui.screens.OCRScreen
import com.textlexiq.ui.screens.ScannerScreen
import com.textlexiq.ui.screens.SettingsScreen
import com.textlexiq.ui.theme.TextLexiqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextLexiqApp()
        }
    }
}

@Composable
fun TextLexiqApp(navController: NavHostController = rememberNavController()) {
    TextLexiqTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(navController = navController)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navigateToScanner = { navController.navigate(Screen.Scanner.route) },
                navigateToOcr = { navController.navigate(Screen.Ocr.route) },
                navigateToDocument = { navController.navigate(Screen.Document.route) },
                navigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onBack = navController::popBackStack,
                onImageCaptured = { capturedPath ->
                    navController.navigate(Screen.Crop.createRoute(capturedPath))
                }
            )
        }
        composable(
            route = Screen.Crop.routeWithArgs(),
            arguments = listOf(
                navArgument(Screen.Crop.imagePathArg) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString(Screen.Crop.imagePathArg)
            if (imagePath != null) {
                CropAndPreviewScreen(
                    imagePath = imagePath,
                    onBack = navController::popBackStack,
                    onNavigateToOcr = { processedPath ->
                        navController.navigate(Screen.Ocr.createRoute(processedPath)) {
                            popUpTo(Screen.Scanner.route) { inclusive = true }
                        }
                    }
                )
            } else {
                navController.popBackStack()
            }
        }
        composable(
            route = Screen.Ocr.routeWithArgs(),
            arguments = listOf(
                navArgument(Screen.Ocr.imagePathArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString(Screen.Ocr.imagePathArg)
            OCRScreen(
                onBack = navController::popBackStack,
                navigateToDocument = { navController.navigate(Screen.Document.route) },
                cleanedImagePath = imagePath
            )
        }
        composable(Screen.Document.route) {
            DocumentViewScreen(onBack = navController::popBackStack)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = navController::popBackStack)
        }
    }
}

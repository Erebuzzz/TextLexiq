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
        val appContainer = (application as TextLexiqApp).container
        setContent {
            TextLexiqApp(userPreferencesRepository = appContainer.userPreferencesRepository)
        }
    }
}

@Composable
fun TextLexiqApp(
    navController: NavHostController = rememberNavController(),
    userPreferencesRepository: com.textlexiq.data.UserPreferencesRepository? = null
) {
    // If repo is provided (from Activity), use it. Otherwise default (preview/test).
    val isDark = userPreferencesRepository?.darkModeEnabled
        ?.androidx.lifecycle.compose.collectAsStateWithLifecycle(initialValue = true)
        ?.value ?: true

    TextLexiqTheme(useDarkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(
                navController = navController,
                userPreferencesRepository = userPreferencesRepository
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    userPreferencesRepository: com.textlexiq.data.UserPreferencesRepository? = null
) {
    val isFirstRun = userPreferencesRepository?.isFirstRun
        ?.androidx.lifecycle.compose.collectAsStateWithLifecycle(initialValue = false)
        ?.value ?: false

    val startDestination = if (isFirstRun) Screen.Onboarding.route else Screen.Home.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            com.textlexiq.ui.screens.OnboardingScreen(
                userPreferencesRepository = userPreferencesRepository!!,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navigateToScanner = { navController.navigate(Screen.Scanner.route) },
                navigateToOcr = { navController.navigate(Screen.Ocr.route) },
                navigateToDocument = { documentId -> 
                    navController.navigate(Screen.Document.createRoute(documentId)) 
                },
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
        composable(
            route = Screen.Document.routeWithArgs(),
            arguments = listOf(
                navArgument(Screen.Document.documentIdArg) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong(Screen.Document.documentIdArg) ?: -1L
            DocumentViewScreen(
                documentId = if (documentId >= 0) documentId else null,
                onBack = navController::popBackStack
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = navController::popBackStack,
                onNavigateToModels = { navController.navigate(Screen.ModelManagement.route) }
            )
        }
        
        composable(Screen.ModelManagement.route) {
            com.textlexiq.ui.screens.ModelManagementScreen(
                onBack = navController::popBackStack
            )
        }
    }
}

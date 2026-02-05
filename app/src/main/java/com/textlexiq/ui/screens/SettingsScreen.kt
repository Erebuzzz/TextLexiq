package com.textlexiq.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.AppViewModelProvider
import com.textlexiq.viewmodel.SettingsAction
import com.textlexiq.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModels: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            com.textlexiq.ui.components.BentoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Premium dark theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.darkModeEnabled,
                        onCheckedChange = { viewModel.onAction(SettingsAction.SetDarkMode(it)) }
                    )
                }
            }

            // Intelligence Section (OCR & LLM)
            com.textlexiq.ui.components.BentoCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Intelligence (AI)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // LLM Toggle
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Cloud Intelligence", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Use paid cloud models for complex tasks. Disable for offline only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.llmTier != com.textlexiq.llm.EngineTier.ON_DEVICE,
                        onCheckedChange = { enabled ->
                            val tier = if (enabled) com.textlexiq.llm.EngineTier.CLOUD_PREMIUM else com.textlexiq.llm.EngineTier.ON_DEVICE
                            viewModel.onAction(SettingsAction.SetLlmTier(tier))
                        }
                    )
                }

                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.material3.OutlinedButton(
                    onClick = onNavigateToModels,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = null)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Offline Models")
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                // OCR Toggle (Placeholder logic)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                     Column {
                        Text(text = "OCR Engine", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (state.ocrEngine == "mlkit") "Google ML Kit (Fast)" else "Tesseract (Robust)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.ocrEngine == "tesseract",
                        onCheckedChange = { enabled ->
                            viewModel.onAction(SettingsAction.SetOcrEngine(if (enabled) "tesseract" else "mlkit"))
                        }
                    )
                }
            }
        }
    }
}

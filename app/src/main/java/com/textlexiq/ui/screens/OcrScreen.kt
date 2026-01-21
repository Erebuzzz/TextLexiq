package com.textlexiq.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.OcrAction
import com.textlexiq.viewmodel.OcrViewModel
import com.textlexiq.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    onDocumentSaved: (String) -> Unit,
    cleanedImagePath: String?,
    viewModel: OcrViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cleanedImagePath) {
        cleanedImagePath?.let { viewModel.onImagePathAvailable(it) }
    }

    LaunchedEffect(state.savedDocumentId) {
        val documentId = state.savedDocumentId ?: return@LaunchedEffect
        onDocumentSaved(documentId)
        viewModel.onNavigationHandled()
    }

    Scaffold(
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isProcessing || state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }

            Text(
                text = "Confidence: ${(state.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = state.editedText,
                onValueChange = { viewModel.onAction(OcrAction.UpdateText(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Extracted Text") },
                singleLine = false,
                minLines = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onAction(OcrAction.SaveDocument) },
                enabled = !state.isProcessing && !state.isSaving
            ) {
                Text(text = "Save Document")
            }

            Button(onClick = { viewModel.onAction(OcrAction.RetryScan) }) {
                Text(text = "Retry")
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

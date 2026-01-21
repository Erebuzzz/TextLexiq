package com.textlexiq.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.Description
import androidx.compose.material3.icons.filled.Settings
import androidx.compose.material3.icons.filled.TextSnippet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.HomeAction
import com.textlexiq.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navigateToScanner: () -> Unit,
    navigateToOcr: () -> Unit,
    navigateToDocument: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "TextLexiq") },
                actions = {
                    IconButton(onClick = navigateToOcr) {
                        Icon(imageVector = Icons.Default.TextSnippet, contentDescription = "OCR")
                    }
                    IconButton(onClick = navigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onAction(HomeAction.StartScan)
                navigateToScanner()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Scan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.recentDocuments.isEmpty()) {
                item {
                    Text(
                        text = state.emptyMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.recentDocuments) { document ->
                    ListItem(
                        headlineContent = { Text(text = document.name) },
                        supportingContent = { Text(text = document.summary) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null
                            )
                        },
                        overlineContent = { Text(text = document.lastUpdatedLabel) },
                        modifier = Modifier.clickable {
                            viewModel.onAction(HomeAction.OpenDocument(document.id))
                            navigateToDocument()
                        }
                    )
                }
            }
        }
    }
}

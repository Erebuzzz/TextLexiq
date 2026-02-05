package com.textlexiq.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.HomeAction
import com.textlexiq.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navigateToScanner: () -> Unit,
    navigateToOcr: () -> Unit,
    navigateToDocument: (Long) -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Using custom CommandBar instead of standard TopBar
        topBar = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable(enabled = false) {} // Consume clicks
            ) {
               // Placeholder for spacing if needed, but CommandBar is inside content or here?
               // Let's put CommandBar in the main content for scrolling or fixed at top.
               // Actually, pinning it to topBar slot is safe.
               com.textlexiq.ui.components.CommandBar(
                   query = searchQuery,
                   onQueryChange = { searchQuery = it },
                   placeholder = "Search documents or type 'Scan'..."
               )
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Bento Grid for Actions & Content
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Feature: New Scan (Span 2 - Full Width Prominence)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                   com.textlexiq.ui.components.GlowButton(
                       text = "✨ Start New Scan",
                       onClick = { 
                           viewModel.onAction(HomeAction.StartScan)
                           navigateToScanner() 
                       },
                       modifier = Modifier.fillMaxWidth(),
                       icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                   )
                }

                // Section Header
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.recentDocuments.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        com.textlexiq.ui.components.BentoCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.emptyMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.grid.items(state.recentDocuments) { document ->
                        com.textlexiq.ui.components.BentoCard(
                            onClick = {
                                viewModel.onAction(HomeAction.OpenDocument(document.id))
                                navigateToDocument(document.id.toLong())
                            }
                        ) {
                            androidx.compose.foundation.layout.Column {
                                Text(
                                    text = document.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = document.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 2
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = document.lastUpdatedLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

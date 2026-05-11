package org.classapp.bookmark.ui.screens.collection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.model.CollectionEntryDetail
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun BookCollectionScreen(
    collectionService: BookCollectionService
) {
    var collectionDetails by remember { mutableStateOf<List<CollectionEntryDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Load data when screen opens
    LaunchedEffect(Unit) {
        try {
            collectionDetails = collectionService.getUserCollectionEntries()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(collectionDetails) { detail ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = detail.book.title, style = MaterialTheme.typography.titleMedium)

                            // DELETE Button
                            IconButton(onClick = {
                                scope.launch {
                                    collectionService.removeBookFromCollection(detail.entry.id)
                                    collectionDetails = collectionService.getUserCollectionEntries()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Text(text = "Author: ${detail.book.authors?.joinToString() ?: "Unknown"}")

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(text = "Progress: ${detail.entry.pageReaded} / ${detail.book.numberOfPage}")
                            Spacer(modifier = Modifier.weight(1f))

                            // CHANGE Button
                            TextButton(onClick = {
                                scope.launch {
                                    val newPage = (detail.entry.pageReaded ?: 0) + 1
                                    // updateReadingStatus(entryId, newStatus, pageReaded)
                                    collectionService.updateReadingStatus(detail.entry.id, null, newPage)
                                    collectionDetails = collectionService.getUserCollectionEntries()
                                }
                            }) {
                                Text("+1 Page")
                            }
                        }

                        val progress = (detail.entry.pageReaded?.toFloat() ?: 0f) / (detail.book.numberOfPage?.toFloat() ?: 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
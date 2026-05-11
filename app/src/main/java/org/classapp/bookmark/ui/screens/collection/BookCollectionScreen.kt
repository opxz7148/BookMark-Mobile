package org.classapp.bookmark.ui.screens.collection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.classapp.bookmark.core.model.CollectionEntryDetail
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun BookCollectionScreen(
    collectionService: BookCollectionService
) {
    var collectionDetails by remember { mutableStateOf<List<CollectionEntryDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            collectionDetails = collectionService.getUserCollectionEntries()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                        Text(text = detail.book.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Author: ${detail.book.authors?.joinToString() ?: "Unknown"}")
                        Text(text = "Progress: ${detail.entry.pageReaded} / ${detail.book.numberOfPage}")

                        // Linear progress bar
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
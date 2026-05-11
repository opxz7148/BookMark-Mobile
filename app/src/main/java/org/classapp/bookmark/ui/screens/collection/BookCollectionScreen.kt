package org.classapp.bookmark.ui.screens.collection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.model.CollectionEntryDetail
import org.classapp.bookmark.core.model.EntryStatus
import org.classapp.bookmark.core.service.BookCollectionService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCollectionScreen(
    collectionService: BookCollectionService
) {
    var collectionDetails by remember { mutableStateOf<List<CollectionEntryDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingDetail by remember { mutableStateOf<CollectionEntryDetail?>(null) }
    val scope = rememberCoroutineScope()

    fun refreshCollection() {
        scope.launch {
            isLoading = true
            try {
                collectionDetails = collectionService.getUserCollectionEntries()
            } finally {
                isLoading = false
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        refreshCollection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Library", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { refreshCollection() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && collectionDetails.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!isLoading && collectionDetails.isEmpty()) {
                Text(
                    "Your collection is empty. Add some books!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(collectionDetails) { detail ->
                        BookCollectionCard(
                            detail = detail,
                            onEdit = { editingDetail = detail },
                            onDelete = {
                                scope.launch {
                                    collectionService.removeBookFromCollection(detail.entry.id)
                                    refreshCollection()
                                }
                            }
                        )
                    }
                }
            }

            if (isLoading && collectionDetails.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }

    // Edit Dialog
    editingDetail?.let { detail ->
        EditEntryDialog(
            detail = detail,
            onDismiss = { editingDetail = null },
            onConfirm = { status, pages ->
                scope.launch {
                    collectionService.updateReadingStatus(detail.entry.id, status, pages)
                    editingDetail = null
                    refreshCollection()
                }
            }
        )
    }
}

@Composable
fun BookCollectionCard(
    detail: CollectionEntryDetail,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detail.book.authors?.joinToString() ?: "Unknown Author",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge with Color
                Surface(
                    color = getStatusColor(detail.status),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = detail.status.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${detail.entry.pageReaded ?: 0} / ${detail.book.numberOfPage ?: 0} pages",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalPages = if ((detail.book.numberOfPage ?: 0) <= 0) 1 else detail.book.numberOfPage!!
            val readPages = detail.entry.pageReaded ?: 0
            val progress = (readPages.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun getStatusColor(status: EntryStatus): Color {
    return when (status) {
        EntryStatus.READING -> Color(0xFF4CAF50) // Green
        EntryStatus.WANT_TO_READ -> Color(0xFF2196F3) // Blue
        EntryStatus.COMPLETED -> Color(0xFF9C27B0) // Purple
        EntryStatus.DROPPED -> Color(0xFFF44336) // Red
        EntryStatus.TBR -> Color(0xFFFF9800) // Orange
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditEntryDialog(
    detail: CollectionEntryDetail,
    onDismiss: () -> Unit,
    onConfirm: (EntryStatus, Int) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(detail.status) }
    var pageReaded by remember { mutableStateOf(detail.entry.pageReaded?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(detail.book.title, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Status", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EntryStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pageReaded,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) pageReaded = it },
                    label = { Text("Pages Read") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("/ ${detail.book.numberOfPage ?: "???"}") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pages = pageReaded.toIntOrNull() ?: 0
                    onConfirm(selectedStatus, pages)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

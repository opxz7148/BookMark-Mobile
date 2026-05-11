package org.classapp.bookmark.ui.screens.collection

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.model.CollectionEntryDetail
import org.classapp.bookmark.core.model.EntryStatus
import org.classapp.bookmark.core.service.BookCollectionService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCollectionScreen(collectionService: BookCollectionService) {
    var collectionDetails by remember { mutableStateOf<List<CollectionEntryDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingDetail by remember { mutableStateOf<CollectionEntryDetail?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    LaunchedEffect(Unit) { refreshCollection() }

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
                Text("Library is empty.", modifier = Modifier.align(Alignment.Center))
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
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    editingDetail?.let { detail ->
        EditEntryDialog(
            detail = detail,
            onDismiss = { editingDetail = null },
            onConfirm = { status, pages ->
                scope.launch {
                    collectionService.updateReadingStatus(detail.entry.id, status, pages)
                    editingDetail = null
                    refreshCollection()
                    Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun BookCollectionCard(detail: CollectionEntryDetail, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = detail.book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    // Source Tag Logic
                    val isManual = detail.book.isbn.isNullOrBlank() || detail.book.isbn == "0"
                    AssistChip(
                        onClick = {},
                        label = { Text(if (isManual) "Manual Entry" else "ISBN: ${detail.book.isbn}") },
                        leadingIcon = { Icon(if (isManual) Icons.Default.Edit else Icons.Default.Search, null, Modifier.size(16.dp)) }
                    )
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = getStatusColor(detail.status), shape = MaterialTheme.shapes.extraSmall) {
                    Text(
                        text = detail.status.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
                Text("${detail.entry.pageReaded ?: 0} / ${detail.book.numberOfPage ?: 0} pages", style = MaterialTheme.typography.bodySmall)
            }

            val progress = ((detail.entry.pageReaded ?: 0).toFloat() / (if ((detail.book.numberOfPage ?: 0) <= 0) 1 else detail.book.numberOfPage!!).toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), strokeCap = StrokeCap.Round)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditEntryDialog(detail: CollectionEntryDetail, onDismiss: () -> Unit, onConfirm: (EntryStatus, Int) -> Unit) {
    var selectedStatus by remember { mutableStateOf(detail.status) }
    var pageReaded by remember { mutableStateOf(detail.entry.pageReaded?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress") },
        text = {
            Column {
                Text(detail.book.title, style = MaterialTheme.typography.titleSmall)
                FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EntryStatus.entries.forEach { status ->
                        FilterChip(selected = selectedStatus == status, onClick = { selectedStatus = status }, label = { Text(status.displayName) })
                    }
                }
                OutlinedTextField(
                    value = pageReaded,
                    onValueChange = { if (it.all { c -> c.isDigit() }) pageReaded = it },
                    label = { Text("Pages Read") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("/ ${detail.book.numberOfPage}") }
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedStatus, pageReaded.toIntOrNull() ?: 0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun getStatusColor(status: EntryStatus): Color = when (status) {
    EntryStatus.READING -> Color(0xFF4CAF50)
    EntryStatus.WANT_TO_READ -> Color(0xFF2196F3)
    EntryStatus.COMPLETED -> Color(0xFF9C27B0)
    EntryStatus.DROPPED -> Color(0xFFF44336)
    EntryStatus.TBR -> Color(0xFFFF9800)
}
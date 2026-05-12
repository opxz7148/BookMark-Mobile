package org.classapp.bookmark.ui.screens.collection

import android.widget.Toast
import androidx.compose.animation.*
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

enum class SortOption(val label: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    AUTHOR_ASC("Writer (A-Z)"),
    PUB_DATE_DESC("Newest Published"),
    DATE_ADDED_DESC("Recently Added")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCollectionScreen(collectionService: BookCollectionService) {
    // 1. STATE DEFINITIONS
    var collectionDetails by remember { mutableStateOf<List<CollectionEntryDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingDetail by remember { mutableStateOf<CollectionEntryDetail?>(null) } // Controls the Dialog

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf(SortOption.DATE_ADDED_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 2. REFRESH LOGIC
    fun refreshCollection() {
        scope.launch {
            isLoading = true
            try {
                collectionDetails = collectionService.getUserCollectionEntries()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshCollection() }

    // 3. FILTERING & SORTING
    val allGenres = remember(collectionDetails) {
        listOf("All") + collectionDetails.flatMap { it.book.genre ?: emptyList() }.distinct().sorted()
    }

    val processedList = remember(collectionDetails, searchQuery, selectedGenre, sortOrder) {
        var list = collectionDetails.filter {
            (it.book.title.contains(searchQuery, ignoreCase = true) ||
                    it.book.authors?.any { a -> a.contains(searchQuery, ignoreCase = true) } == true) &&
                    (selectedGenre == "All" || it.book.genre?.contains(selectedGenre) == true)
        }
        when (sortOrder) {
            SortOption.TITLE_ASC -> list = list.sortedBy { it.book.title }
            SortOption.TITLE_DESC -> list = list.sortedByDescending { it.book.title }
            SortOption.AUTHOR_ASC -> list = list.sortedBy { it.book.authors?.firstOrNull() ?: "" }
            SortOption.PUB_DATE_DESC -> list = list.sortedByDescending { it.book.pubDate ?: "" }
            SortOption.DATE_ADDED_DESC -> list = list.sortedByDescending { it.entry.id }
        }
        list
    }

    // 4. UI LAYOUT
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Library", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showSortMenu = true }) { Icon(Icons.Default.Sort, "Sort") }
                        IconButton(onClick = { showFilterMenu = true }) { Icon(Icons.Default.FilterList, "Filter") }
                        IconButton(onClick = { refreshCollection() }) { Icon(Icons.Default.Refresh, "Refresh") }
                    }
                )

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search title or author...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.label) }, onClick = { sortOrder = option; showSortMenu = false })
                    }
                }
                DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                    allGenres.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre) },
                            onClick = { selectedGenre = genre; showFilterMenu = false },
                            trailingIcon = { if (genre == selectedGenre) Icon(Icons.Default.Check, null) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!isLoading && processedList.isEmpty()) {
                Text("No books found.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(processedList) { detail ->
                        BookCollectionCard(
                            detail = detail,
                            onEdit = { editingDetail = detail }, // THIS triggers the dialog
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
        }
    }

    // 5. THE DIALOG (Must be outside Scaffold but inside the Screen Composable)
    editingDetail?.let { detail ->
        EditEntryDialog(
            detail = detail,
            onDismiss = { editingDetail = null },
            onConfirm = { status, pages ->
                scope.launch {
                    collectionService.updateReadingStatus(detail.entry.id, status, pages)
                    editingDetail = null
                    refreshCollection()
                    Toast.makeText(context, "Update successful!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun BookCollectionCard(detail: CollectionEntryDetail, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isManual = detail.book.isbn == "MANUAL" || detail.book.isbn.isNullOrBlank()

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = detail.book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isManual) "Added by: ${detail.book.authors?.firstOrNull() ?: "User"}"
                        else detail.book.authors?.joinToString() ?: "Unknown Author",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Published: ${detail.book.pubDate ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Text("Genre: ${detail.book.genre?.joinToString() ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Text("ISBN: ${detail.book.isbn ?: "Manual Entry"}", style = MaterialTheme.typography.bodySmall)
                    Text("Description: ${detail.book.description ?: "No description available."}",
                        style = MaterialTheme.typography.bodySmall, maxLines = 3)
                }
            }

            val total = if ((detail.book.numberOfPage ?: 0) <= 0) 1 else detail.book.numberOfPage!!
            val progress = ((detail.entry.pageReaded ?: 0).toFloat() / total.toFloat()).coerceIn(0f, 1f)

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(color = getStatusColor(detail.status), shape = MaterialTheme.shapes.extraSmall) {
                    Text(detail.status.displayName, modifier = Modifier.padding(4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Text("${detail.entry.pageReaded} / ${detail.book.numberOfPage} pages", style = MaterialTheme.typography.bodySmall)
            }

            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), strokeCap = StrokeCap.Round)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                // IMPORTANT: This triggers the onEdit() passed from parent
                TextButton(onClick = onEdit) { Text("Update Progress") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
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
                Text(detail.book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EntryStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = pageReaded,
                    onValueChange = { if (it.all { c -> c.isDigit() }) pageReaded = it },
                    label = { Text("Pages Read") },
                    suffix = { Text("/ ${detail.book.numberOfPage}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedStatus, pageReaded.toIntOrNull() ?: 0) }) {
                Text("Save Changes")
            }
        },
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
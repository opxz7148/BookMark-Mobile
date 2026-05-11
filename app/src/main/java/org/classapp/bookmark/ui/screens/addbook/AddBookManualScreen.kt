package org.classapp.bookmark.ui.screens.addbook

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun AddBookManualScreen(
    collectionService: BookCollectionService,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Manual Entry", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Book Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pages,
            onValueChange = { if (it.all { c -> c.isDigit() }) pages = it },
            label = { Text("Total Pages") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && !isSaving,
            onClick = {
                isSaving = true
                scope.launch {
                    try {
                        // We provide EVERY field to ensure the Service doesn't hang
                        collectionService.addBookToCollectionByInput(
                            title = title.trim(),
                            numberOfPage = pages.toIntOrNull() ?: 0,
                            subTitle = "",
                            description = "Manually added book",
                            isbn = "", // Explicitly empty to avoid null issues
                            authors = if (author.isBlank()) emptyList() else listOf(author.trim()),
                            pubDate = "",
                            genre = emptyList(),
                            status = org.classapp.bookmark.core.model.EntryStatus.WANT_TO_READ.name
                        )

                        android.widget.Toast.makeText(context, "Success!", android.widget.Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } catch (e: Exception) {
                        // This will now tell us the REAL reason (e.g. Permission Denied)
                        android.util.Log.e("MANUAL_ADD_ERROR", "Failed: ${e.message}")
                        android.widget.Toast.makeText(context, "Error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    } finally {
                        isSaving = false
                    }
                }
            }
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Save to Library")
        }
    }
}
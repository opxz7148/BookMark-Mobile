package org.classapp.bookmark.ui.screens.addbook

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun AddBookISBNScreen(
    collectionService: BookCollectionService,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isbnQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add via ISBN", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = isbnQuery,
            onValueChange = { isbnQuery = it },
            label = { Text("ISBN-10 or ISBN-13") },
            placeholder = { Text("e.g. 9780441172719") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isbnQuery.isNotBlank() && !isLoading,
            onClick = {
                isLoading = true
                scope.launch {
                    try {
                        collectionService.addBookToCollectionByISBN(isbnQuery.trim())
                        Toast.makeText(context, "Book Added Successfully!", Toast.LENGTH_SHORT).show()
                        onSuccess() // Switches tab back to Collection
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Search & Add to Library")
            }
        }
    }
}
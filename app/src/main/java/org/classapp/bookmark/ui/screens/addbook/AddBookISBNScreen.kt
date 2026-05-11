package org.classapp.bookmark.ui.screens.addbook

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun AddBookISBNScreen(
    collectionService: BookCollectionService,
    onSuccess: () -> Unit
) {
    var isbnQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add via ISBN", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = isbnQuery,
            onValueChange = { isbnQuery = it },
            label = { Text("ISBN Number") },
            placeholder = { Text("e.g. 9780441172719") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isbnQuery.isNotBlank() && !isLoading,
            onClick = {
                isLoading = true
                errorMessage = null
                scope.launch {
                    try {
                        // Using the verified function name from your service
                        collectionService.addBookToCollectionByISBN(isbnQuery.trim())
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e("ISBN_ERROR", "Search failed: ${e.message}")
                        errorMessage = "Error: ${e.localizedMessage ?: "Book not found"}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Search & Add to Collection")
            }
        }
    }
}
package org.classapp.bookmark.ui.screens.addbook

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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Search by ISBN",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Enter the 10 or 13-digit ISBN number found on the back of the book.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = isbnQuery,
            onValueChange = { isbnQuery = it },
            label = { Text("ISBN Number") },
            placeholder = { Text("e.g. 9780134685991") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
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
                        collectionService.addBookToCollectionByISBN(isbnQuery.trim())
                        onSuccess()
                    } catch (e: Exception) {
                        errorMessage = "Could not find book. Please check the ISBN or try manual entry."
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Search & Add Book")
            }
        }
    }
}
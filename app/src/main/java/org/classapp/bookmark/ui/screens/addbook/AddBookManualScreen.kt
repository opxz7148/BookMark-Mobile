package org.classapp.bookmark.ui.screens.addbook

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.classapp.bookmark.core.service.BookCollectionService

@Composable
fun AddBookManualScreen(
    collectionService: BookCollectionService,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Manual Entry", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Total Pages") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    try {
                        collectionService.addBookToCollectionByInput(
                            title = title,
                            authors = listOf(author),
                            numberOfPage = pages.toIntOrNull() ?: 0
                        )
                        onSuccess()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        ) {
            Text("Save to Collection")
        }
    }
}
package org.classapp.bookmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dagger.hilt.android.AndroidEntryPoint
import org.classapp.bookmark.core.service.BookCollectionService
import org.classapp.bookmark.core.service.UserService
import org.classapp.bookmark.ui.screens.addbook.AddBookISBNScreen
import org.classapp.bookmark.ui.screens.auth.LoginRegisterScreen
import org.classapp.bookmark.ui.screens.collection.BookCollectionScreen
import org.classapp.bookmark.ui.screens.profile.ProfileScreen
import org.classapp.bookmark.ui.theme.BookMarkTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var collectionService: BookCollectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BookMarkTheme {
                val userState by userService.currentUser.collectAsState(initial = null)
                val hasUser = userService.hasUser()

                if (hasUser || userState != null) {
                    BookMarkApp(userService, collectionService)
                } else {
                    LoginRegisterScreen(
                        userService = userService,
                        onLoginSuccess = {
                            // User state will update via flow
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BookMarkApp(userService: UserService, collectionService: BookCollectionService) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.COLLECTION) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.COLLECTION -> BookCollectionScreen(collectionService)
                    AppDestinations.ADD_BOOK -> AddBookISBNScreen(collectionService)
                    AppDestinations.PROFILE -> ProfileScreen(userService)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    COLLECTION("Collection", Icons.Default.List),
    ADD_BOOK("Add Book", Icons.Default.Add),
    PROFILE("Profile", Icons.Default.AccountBox),
}

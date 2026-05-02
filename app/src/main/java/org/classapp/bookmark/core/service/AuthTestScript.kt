package org.classapp.bookmark.core.service

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Simple standalone test script for Firebase auth flow.
 * Run from AndroidStudio or command line to verify:
 * 1. Firebase initialization
 * 2. User signup with display name
 * 3. User signin
 *
 * Usage: Call AuthTestScript.test(context) from MainActivity or a test
 */
object AuthTestScript {
    private const val TAG = "AuthTestScript"

    fun createFirebaseApp(context: Context): FirebaseApp? {
        return try {
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isEmpty()) {
                Log.d(TAG, "No existing Firebase apps, initializing...")
                val defaultApp = FirebaseApp.initializeApp(context)
                if (defaultApp != null) {
                    Log.d(TAG, "Firebase initialized: ${defaultApp.name}")
                    Log.d(TAG, "Project: ${defaultApp.options?.projectId}")
                } else {
                    Log.e(TAG, "Firebase initializeApp returned null")
                }
                defaultApp
            } else {
                Log.d(TAG, "Firebase already initialized (${existingApps.size} app(s))")
                val defaultApp = FirebaseApp.getInstance()
                Log.d(TAG, "Default app: ${defaultApp.name}, Project: ${defaultApp.options?.projectId}")
                defaultApp
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init failed", e)
            e.printStackTrace()
            null
        }
    }

    fun test(context: Context) = runBlocking {

        createFirebaseApp(context)

        // Step 1: Create UserService
        Log.d(TAG, "Creating UserService...")
        val userService = UserService()

        val testEmail = "testuser_${System.currentTimeMillis()}@test.com"
        val testPassword = "Test@12345"
        val testDisplayName = "Test User ${System.currentTimeMillis()}"

        try {
            // Step 2: Signup
            Log.d(TAG, "Step 2: Attempting signup...")
            userService.signUp(testEmail, testPassword, testDisplayName)
            Log.d(TAG, "Signup successful")

            // Step 3: Get current user
            Log.d(TAG, "Step 3: Getting current user...")
            val userId = userService.currentUserId
            Log.d(TAG, "Current user ID: $userId")

            // Step 4: Signout
            Log.d(TAG, "Step 4: Signing out...")
            userService.signOut()
            Log.d(TAG, "Signout successful")

            // Step 5: Signin
            Log.d(TAG, "Step 5: Attempting signin...")
            userService.signIn(testEmail, testPassword)
            Log.d(TAG, "Signin successful")

            // Step 6: Get current user again
            Log.d(TAG, "Step 6: Getting current user after signin...")
            val userId2 = userService.currentUserId
            Log.d(TAG, "Current user ID after signin: $userId2")

            Log.d(TAG, "========== AUTH TEST SCRIPT PASSED ==========")
        } catch (e: Exception) {
            Log.e(TAG, "TEST FAILED: ${e.message}", e)
            e.printStackTrace()
            Log.d(TAG, "========== AUTH TEST SCRIPT FAILED ==========")
        }
    }

    fun testBookService(context: Context) = runBlocking {
        createFirebaseApp(context)
        val bookService = BookService()
        try {
            Log.d(TAG, "Testing BookService...")
            Log.d(TAG, "Adding test book...")
            bookService.createBookFromInput(
                title = "Test Book",
                numberOfPage = 123,
                subTitle = "A Subtitle",
                description = "This is a test book created by AuthTestScript.",
                isbn = "1234567890",
                authors = "Test Author",
                pubDate = "2024-01-01",
                genre = "Test Genre"
            )
            Log.d(TAG, "Book added successfully")
            Log.d(TAG, "========== BOOK SERVICE TEST PASSED ==========")
        } catch (e: Exception) {
            Log.e(TAG, "BOOK SERVICE TEST FAILED: ${e.message}", e)
            e.printStackTrace()
            Log.d(TAG, "========== BOOK SERVICE TEST FAILED ==========")
        }
    }
}



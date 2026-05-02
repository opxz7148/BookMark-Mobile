package org.classapp.bookmark.scripts

import kotlinx.coroutines.runBlocking
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.classapp.bookmark.core.service.UserService
import android.content.Context

/**
 * Standalone Firebase Auth Testing Script
 *
 * This script tests UserService with REAL Firebase integration
 * WITHOUT running the full Android app.
 *
 * It tests:
 * - Firebase initialization
 * - User signup
 * - User signin
 * - User signout
 * - Display name setting
 *
 * Run with: ./gradlew installDebugDebug then manually call from test
 * Or run as instrumented test: ./gradlew connectedAndroidTest
 */

fun main(args: Array<String>) {
    // Note: This needs Android context, so it must be run as instrumented test
    println("This script must be run as an instrumented test.")
    println("Use: ./gradlew connectedAndroidTest")
    println("Or run FirebaseAuthTestScript.kt as an instrumented test")
}

/**
 * Standalone Firebase Integration Test
 *
 * Usage:
 * 1. Create as instrumented test in androidTest folder
 * 2. Run with: ./gradlew connectedAndroidTest
 */
class FirebaseAuthIntegrationTest(private val context: android.app.Application) {

    private val userService = UserService()
    private val testEmail = "test_${System.currentTimeMillis()}@example.com"
    private val testPassword = "Test@12345"
    private val testDisplayName = "Test User"

    fun runAllTests() = runBlocking {
        println("\n========== FIREBASE AUTH INTEGRATION TEST ==========\n")

        try {
            // Initialize Firebase
            println("Step 1: Initialize Firebase")
            initializeFirebase()
            println("✓ Firebase initialized\n")

            // Test Signup
            println("Step 2: Test Signup")
            testSignup()
            println("✓ Signup successful\n")

            // Test Get Current User
            println("Step 3: Test Get Current User")
            testGetCurrentUser()
            println("✓ Current user retrieved\n")

            // Test Update Profile
            println("Step 4: Test Update Profile (Display Name)")
            testUpdateProfile()
            println("✓ Profile updated\n")

            // Test Signout
            println("Step 5: Test Signout")
            testSignout()
            println("✓ Signout successful\n")

            // Test Signin
            println("Step 6: Test Signin")
            testSignin()
            println("✓ Signin successful\n")

            println("========== ALL TESTS PASSED ==========")
            println("Result: Firebase integration working correctly!")

        } catch (e: Exception) {
            println("\n❌ TEST FAILED: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            throw e
        }
    }

    private fun initializeFirebase() {
        val existingApps = FirebaseApp.getApps(context)
        if (existingApps.isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        println("  Firebase app initialized: ${FirebaseApp.getInstance().name}")
    }

    private suspend fun testSignup() {
        println("  Email: $testEmail")
        println("  Password: $testPassword")
        println("  Display Name: $testDisplayName")

        try {
            userService.signUp(testEmail, testPassword, testDisplayName)
            println("  ✓ User created with email and password")
        } catch (e: Exception) {
            throw Exception("Signup failed: ${e.message}", e)
        }
    }

    private fun testGetCurrentUser() {
        val userId = userService.currentUserId
        if (userId.isEmpty()) {
            throw Exception("No current user found after signup")
        }
        println("  Current user ID: $userId")
        println("  User is logged in: ${userService.hasUser()}")
    }

    private suspend fun testUpdateProfile() {
        // The profile should already be updated during signup
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            throw Exception("No current user found")
        }

        println("  Display Name: ${currentUser.displayName}")
        println("  Email: ${currentUser.email}")

        if (currentUser.displayName == null) {
            throw Exception("Display name not set")
        }
    }

    private fun testSignout() {
        userService.signOut()

        val userId = userService.currentUserId
        if (userId.isNotEmpty()) {
            throw Exception("User not signed out properly")
        }
        println("  User signed out successfully")
    }

    private suspend fun testSignin() {
        println("  Email: $testEmail")
        println("  Password: $testPassword")

        try {
            userService.signIn(testEmail, testPassword)
            println("  ✓ User signed in successfully")
        } catch (e: Exception) {
            throw Exception("Signin failed: ${e.message}", e)
        }

        val userId = userService.currentUserId
        if (userId.isEmpty()) {
            throw Exception("User not signed in properly")
        }
        println("  Current user ID: $userId")
    }
}


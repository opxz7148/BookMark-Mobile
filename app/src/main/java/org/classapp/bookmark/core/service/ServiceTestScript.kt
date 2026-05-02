package org.classapp.bookmark.core.service

import android.util.Log
import kotlinx.coroutines.runBlocking

/**
 * Standalone UserService Test Script
 *
 * This script tests UserService with Firebase integration
 * WITHOUT depending on the full app or any UI components.
 *
 * It can be called from:
 * 1. MainActivity or any activity
 * 2. Application.onCreate()
 * 3. Debug configuration
 *
 * Usage:
 *   ServiceTestScript.runTests(context)
 *
 * The script will:
 * - Initialize Firebase
 * - Create a test user via signup
 * - Verify user data
 * - Test signin/signout
 * - Log all results
 */
object ServiceTestScript {
    private const val TAG = "ServiceTestScript"

    fun runTests() = runBlocking {
        println("\n========== USER SERVICE FIREBASE TEST ==========\n")
        Log.d(TAG, "========== USER SERVICE FIREBASE TEST ==========")

        val userService = UserService()
        val testEmail = "firebase_test_${System.currentTimeMillis()}@test.com"
        val testPassword = "Test@12345"
        val testDisplayName = "Firebase Test User"

        try {
            // Step 1: Test Signup
            println("\nStep 1: Testing Signup")
            Log.d(TAG, "Step 1: Testing Signup")
            println("  Email: $testEmail")
            println("  Password: $testPassword")
            println("  Display Name: $testDisplayName")

            userService.signUp(testEmail, testPassword, testDisplayName)
            println("  ✓ Signup successful")
            Log.d(TAG, "✓ Signup successful")

            // Step 2: Get Current User ID
            println("\nStep 2: Get Current User ID")
            Log.d(TAG, "Step 2: Get Current User ID")

            val userId = userService.currentUserId
            println("  Current User ID: $userId")
            println("  Has User: ${userService.hasUser()}")
            Log.d(TAG, "Current User ID: $userId")
            Log.d(TAG, "Has User: ${userService.hasUser()}")

            if (userId.isEmpty()) {
                throw Exception("No current user found after signup")
            }
            println("  ✓ Current user verified")
            Log.d(TAG, "✓ Current user verified")

            // Step 3: Test Signout
            println("\nStep 3: Testing Signout")
            Log.d(TAG, "Step 3: Testing Signout")

            userService.signOut()
            val userIdAfterSignout = userService.currentUserId

            println("  User ID after signout: '$userIdAfterSignout'")
            println("  Has User after signout: ${userService.hasUser()}")
            Log.d(TAG, "User ID after signout: '$userIdAfterSignout'")
            Log.d(TAG, "Has User after signout: ${userService.hasUser()}")

            if (userIdAfterSignout.isNotEmpty()) {
                throw Exception("User still logged in after signout")
            }
            println("  ✓ Signout successful")
            Log.d(TAG, "✓ Signout successful")

            // Step 4: Test Signin
            println("\nStep 4: Testing Signin")
            Log.d(TAG, "Step 4: Testing Signin")
            println("  Email: $testEmail")
            println("  Password: $testPassword")

            userService.signIn(testEmail, testPassword)
            println("  ✓ Signin successful")
            Log.d(TAG, "✓ Signin successful")

            // Step 5: Verify User After Signin
            println("\nStep 5: Verify User After Signin")
            Log.d(TAG, "Step 5: Verify User After Signin")

            val userIdAfterSignin = userService.currentUserId
            println("  Current User ID: $userIdAfterSignin")
            println("  Has User: ${userService.hasUser()}")
            Log.d(TAG, "Current User ID: $userIdAfterSignin")
            Log.d(TAG, "Has User: ${userService.hasUser()}")

            if (userIdAfterSignin.isEmpty()) {
                throw Exception("No current user found after signin")
            }
            println("  ✓ User verified after signin")
            Log.d(TAG, "✓ User verified after signin")

            // Step 6: Final Signout
            println("\nStep 6: Final Signout")
            Log.d(TAG, "Step 6: Final Signout")

            userService.signOut()
            println("  ✓ Final signout successful")
            Log.d(TAG, "✓ Final signout successful")

            // Summary
            println("\n========== ALL TESTS PASSED ==========")
            println("Summary:")
            println("✓ Signup with email, password, and display name")
            println("✓ Retrieve current user ID")
            println("✓ Sign out from account")
            println("✓ Sign in again with same credentials")
            println("✓ All UserService methods working correctly with Firebase")

            Log.d(TAG, "========== ALL TESTS PASSED ==========")
            Log.d(TAG, "All UserService methods working correctly")

        } catch (e: Exception) {
            println("\n❌ TEST FAILED")
            println("Error: ${e.message}")
            println("\nStack Trace:")

            Log.e(TAG, "TEST FAILED: ${e.message}", e)
            e.printStackTrace()

            throw e
        }
    }
}



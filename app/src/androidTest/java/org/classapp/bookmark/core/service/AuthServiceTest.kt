package org.classapp.bookmark.core.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import android.util.Log

/**
 * Unit tests for UserService authentication flow.
 *
 * These are instrumented tests that run on an Android device/emulator.
 * They test real Firebase authentication without manual intervention.
 *
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class AuthServiceTest {
    private lateinit var context: Context
    private lateinit var userService: UserService
    private val TAG = "AuthServiceTest"

    private lateinit var testEmail: String
    private val testPassword = "Test@12345"
    private lateinit var testDisplayName: String

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        userService = UserService()

        // Generate unique test credentials
        val timestamp = System.currentTimeMillis()
        testEmail = "testuser_$timestamp@test.com"
        testDisplayName = "Test User $timestamp"

        // Initialize Firebase if not already done
        val existingApps = FirebaseApp.getApps(context)
        if (existingApps.isEmpty()) {
            FirebaseApp.initializeApp(context)
            Log.d(TAG, "Firebase initialized")
        }
    }

    /**
     * Test signup functionality.
     * Verifies that a new user can be created with email, password, and display name.
     */
    @Test
    fun testSignup() = runBlocking {
        Log.d(TAG, "Testing signup with email: $testEmail")

        // Perform signup
        userService.signUp(testEmail, testPassword, testDisplayName)
        Log.d(TAG, "Signup completed")

        // Verify user is logged in
        val currentUserId = userService.currentUserId
        assertTrue("User should be logged in after signup", currentUserId.isNotEmpty())
        assertTrue("Current user should be available", userService.hasUser())
        Log.d(TAG, "Signup test passed. User ID: $currentUserId")
    }

    /**
     * Test getting current user after signup.
     * Verifies that currentUserId returns a valid ID after signup.
     */
    @Test
    fun testGetCurrentUserAfterSignup() = runBlocking {
        // Signup
        userService.signUp(testEmail, testPassword, testDisplayName)

        // Get current user
        val userId = userService.currentUserId
        assertNotNull("User ID should not be null", userId)
        assertFalse("User ID should not be empty", userId.isEmpty())
        assertTrue("hasUser() should return true", userService.hasUser())

        Log.d(TAG, "Get current user test passed. User ID: $userId")
    }

    /**
     * Test signout functionality.
     * Verifies that user is properly logged out.
     */
    @Test
    fun testSignout() = runBlocking {
        // Signup first
        userService.signUp(testEmail, testPassword, testDisplayName)
        val userIdBeforeSignout = userService.currentUserId
        assertTrue("User should be logged in", userIdBeforeSignout.isNotEmpty())

        // Signout
        userService.signOut()

        // Verify user is logged out
        val userIdAfterSignout = userService.currentUserId
        assertTrue("User ID should be empty after signout", userIdAfterSignout.isEmpty())
        assertFalse("hasUser() should return false", userService.hasUser())

        Log.d(TAG, "Signout test passed")
    }

    /**
     * Test signin functionality.
     * Verifies that a user can sign in with email and password.
     */
    @Test
    fun testSignin() = runBlocking {
        // Signup first
        userService.signUp(testEmail, testPassword, testDisplayName)

        // Signout
        userService.signOut()
        val userIdAfterSignout = userService.currentUserId
        assertTrue("User should be signed out", userIdAfterSignout.isEmpty())

        // Signin
        userService.signIn(testEmail, testPassword)

        // Verify user is logged in
        val userIdAfterSignin = userService.currentUserId
        assertTrue("User should be logged in after signin", userIdAfterSignin.isNotEmpty())
        assertTrue("hasUser() should return true", userService.hasUser())

        Log.d(TAG, "Signin test passed. User ID: $userIdAfterSignin")
    }

    /**
     * Test complete authentication flow.
     * Tests signup -> signout -> signin sequence.
     */
    @Test
    fun testCompleteAuthFlow() = runBlocking {
        Log.d(TAG, "Starting complete auth flow test")

        // Step 1: Signup
        Log.d(TAG, "Step 1: Signup")
        userService.signUp(testEmail, testPassword, testDisplayName)
        val userIdAfterSignup = userService.currentUserId
        assertTrue("User should be logged in after signup", userIdAfterSignup.isNotEmpty())

        // Step 2: Signout
        Log.d(TAG, "Step 2: Signout")
        userService.signOut()
        val userIdAfterSignout = userService.currentUserId
        assertTrue("User should be logged out", userIdAfterSignout.isEmpty())

        // Step 3: Signin
        Log.d(TAG, "Step 3: Signin")
        userService.signIn(testEmail, testPassword)
        val userIdAfterSignin = userService.currentUserId
        assertTrue("User should be logged in after signin", userIdAfterSignin.isNotEmpty())

        // Verify both user IDs are the same
        assertEquals("User IDs should match", userIdAfterSignup, userIdAfterSignin)

        Log.d(TAG, "Complete auth flow test passed")
    }

    @After
    fun tearDown() = runBlocking {
        // Clean up: sign out
        try {
            userService.signOut()
            Log.d(TAG, "Test cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}



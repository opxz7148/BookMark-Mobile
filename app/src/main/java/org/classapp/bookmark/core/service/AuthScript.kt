package org.classapp.bookmark.core.service

import android.util.Log

object AuthScript {
    private const val TAG = "AuthScript"

    suspend fun run(
        userService: UserService,
        email: String,
        password: String,
        displayName: String
    ) {
        try {
            Log.d(TAG, "Step 1: signUp")
            userService.signUp(email, password, displayName)

            Log.d(TAG, "Step 2: signOut")
            userService.signOut()

            Log.d(TAG, "Step 3: signIn")
            userService.signIn(email, password)

            Log.d(TAG, "Auth script completed")
        } catch (e: Exception) {
            Log.e(TAG, "Auth script failed: ${e.message}", e)
        }
    }
}


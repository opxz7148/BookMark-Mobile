package org.classapp.bookmark

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookMarkApplication : Application() {
    companion object {
        private const val TAG = "BookMarkApplication"
    }

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            Log.d(TAG, "Starting Firebase initialization...")

            // Check if already initialized
            val existingApps = FirebaseApp.getApps(this)
            Log.d(TAG, "Existing Firebase apps count: ${existingApps.size}")

            if (existingApps.isEmpty()) {
                Log.d(TAG, "No Firebase apps found, attempting initialization...")

                // Try to initialize from google-services.json (automatic)
                try {
                    val defaultApp = FirebaseApp.initializeApp(this)
                    if (defaultApp != null) {
                        Log.d(TAG, "✓ Firebase initialized from google-services.json")
                        Log.d(TAG, "  App name: ${defaultApp.name}")
                        Log.d(TAG, "  Project ID: ${defaultApp.options.projectId}")
                        return
                    } else {
                        Log.w(TAG, "FirebaseApp.initializeApp() returned null")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Automatic Firebase initialization failed: ${e.message}")
                    throw RuntimeException("Firebase initialization failed", e)
                }
            } else {
                Log.d(TAG, "Firebase already initialized via content provider")
                try {
                    val defaultApp = FirebaseApp.getInstance()
                    Log.d(TAG, "✓ Default Firebase app verified")
                    Log.d(TAG, "  App name: ${defaultApp.name}")
                    Log.d(TAG, "  Project ID: ${defaultApp.options.projectId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting default Firebase app: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal Firebase initialization error: ${e.message}", e)
            e.printStackTrace()
        }
    }
}


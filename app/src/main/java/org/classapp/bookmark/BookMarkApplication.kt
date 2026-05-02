package org.classapp.bookmark

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

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
                    Log.w(TAG, "Attempting manual initialization with hardcoded config...")

                    // Fallback: Manual configuration as last resort
                    val options = FirebaseOptions.Builder()
                        .setProjectId("bookmark-f24d6")
                        .setApplicationId("1:983319974099:android:42f7c5bc09a7ee7142c426")
                        .setApiKey("REMOVED")
                        .setDatabaseUrl("https://bookmark-f24d6.firebasedatabase.app")
                        .setStorageBucket("bookmark-f24d6.firebasestorage.app")
                        .build()

                    val manualApp = FirebaseApp.initializeApp(this, options)
                    Log.d(TAG, "✓ Firebase initialized with manual configuration")
                    Log.d(TAG, "  App name: ${manualApp.name}")
                    Log.d(TAG, "  Project ID: ${manualApp.options.projectId}")
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


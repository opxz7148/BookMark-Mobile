package org.classapp.bookmark.core.service.booksoure

import android.util.Log
import org.classapp.bookmark.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleBookAPIInstance {
    val BASE_URL = "https://www.googleapis.com/books/v1/"
    val API_KEY = BuildConfig.GOOGLE_BOOKS_API_KEY
    private const val TAG = "GoogleBookAPI"

    val api: GoogleBooksApi by lazy {
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with logging
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApi::class.java)
    }

    suspend fun fetchBookFromISBN(isbn: String): GoogleBooklistResponse {
        Log.d(TAG, "📤 Requesting: Search books by ISBN: $isbn")
        Log.d(TAG, "🔑 API Key: ${API_KEY.take(10)}...") // Show partial key for security

        return try {
            val response = api.searchByIsbn("isbn:${isbn}", API_KEY)
            Log.d(TAG, "✅ Response received: Found ${response.items?.size ?: 0} books")
            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching books by ISBN: ${e.message}", e)
            throw e
        }
    }

    suspend fun fetchBookDetailById(id: String): GoogleBookDetailResponse {
        Log.d(TAG, "📤 Requesting: Book details for ID: $id")

        return try {
            val response = api.getVolumeDetail(id, API_KEY)
            Log.d(TAG, "✅ Response received: ${response.volumeInfo?.title}")
            Log.d(TAG, "   📖 Pages: ${response.volumeInfo?.pageCount}")
            Log.d(TAG, "   ✍️  Authors: ${response.volumeInfo?.authors?.joinToString(", ")}")
            Log.d(TAG, "   📅 Published: ${response.volumeInfo?.publishedDate}")
            Log.d(TAG, "   🏷️  ISBN: ${response.volumeInfo?.industryIdentifiers?.firstOrNull()?.identifier}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching book details: ${e.message}", e)
            throw e
        }
    }
}
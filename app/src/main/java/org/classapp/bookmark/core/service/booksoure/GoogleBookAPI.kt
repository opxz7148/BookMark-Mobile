package org.classapp.bookmark.core.service.booksoure

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")                     // HTTP GET → openlibrary.org/volumes
    suspend fun searchByIsbn(
        @Query("q") isbn: String,        // adds ?q=isbn:xxx to the URL
        @Query("key") apiKey: String       // adds &key=API_KEY to the URL
    ): GoogleBooklistResponse              // response maps to this data class

    @GET("volumes/{id}")                // HTTP GET → openlibrary.org/volumes/e_9MDwAAQBAJ
    suspend fun getVolumeDetail(
        @Path("id") id: String,          // replaces {id} in the URL with actual value
        @Query("key") apiKey: String
    ): GoogleBookDetailResponse
}
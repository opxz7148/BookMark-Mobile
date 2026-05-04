package org.classapp.bookmark.core.model

data class Book (
    val id: String = "",
    val title: String = "",
    val subTitle: String? = "",
    val description: String? = "",
    val numberOfPage: Int? = 0,
    val isbn: String? = "",
    val authors: List<String>? = emptyList(),
    val pubDate: String? = "",
    val genre: List<String>? = emptyList(),
    val coverImageUrl: List<String>? = emptyList()
) {
    fun getThumbnailUrl(): String = coverImageUrl?.firstOrNull()?: ""   // smallest for list
    fun getBestUrl(): String = coverImageUrl?.lastOrNull()?: ""          // biggest for detail
}
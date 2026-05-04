package org.classapp.bookmark.core.service.booksoure

import org.classapp.bookmark.core.exception.FailedToFetchBook
import org.classapp.bookmark.core.model.Book

data class GoogleBookDetailResponse(
    val id: String? = "",
    val volumeInfo: VolumeInfo? = null
) {
    fun toBook(): Book {
        return volumeInfo?.toBookWithNoId()?.copy(id = id ?: "") ?: throw FailedToFetchBook("Failed to fetch book details from Google Books API: Missing volumeInfo")
    }
}

data class VolumeInfo(
    val title: String? = "",
    val authors: List<String>? = emptyList(),
    val publishedDate: String? = "",
    val description: String? = "",
    val industryIdentifiers: List<IndustryIdentifier>? = emptyList(),
    val pageCount: Int? = 0,
    val categories: List<String>? = emptyList(),
    val imageLinks: ImageLinks? = null
) {
    fun getIsbn(): String {
        val isbn13 = industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
        if (!isbn13.isNullOrEmpty()) {
            return isbn13
        }
        val isbn10 = industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
        if (!isbn10.isNullOrEmpty()) {
            return isbn10
        }
        return ""
    }

    fun parseGenres(): List<String> {
        // The categories from Google Books API are often in the format "Fiction / Science Fiction / Alien Contact"
        // We can split them by " / " to get more granular genres
        val genres = categories?.flatMap { it.split(" / ") }?.map { it.trim() }?.filter { it.isNotEmpty() }?.distinct()
        return genres ?: emptyList()
    }

    fun toBookWithNoId(): Book {
        return Book(
            title = title ?: "",
            subTitle = "", // Google Books API does not have a separate subtitle field
            description = description ?: "",
            numberOfPage = pageCount ?: 0,
            isbn = getIsbn(),
            authors = authors ?: emptyList(),
            pubDate = publishedDate ?: "",
            genre = parseGenres(),
            coverImageUrl = imageLinks?.toSortedList() ?: emptyList()
        )
    }
}

data class IndustryIdentifier(
    val type: String? = "",
    val identifier: String? = ""
)

data class ImageLinks(
    val smallThumbnail: String? = "",
    val thumbnail: String? = "",
    val small: String? = "",
    val medium: String? = "",
    val large: String? = "",
    val extraLarge: String? = ""
) {
    fun toSortedList(): List<String> {
        val urls = listOfNotNull(
            smallThumbnail,
            thumbnail,
            small,
            medium,
            large,
            extraLarge
        ).filter { it.isNotEmpty() }
        return urls
    }
}




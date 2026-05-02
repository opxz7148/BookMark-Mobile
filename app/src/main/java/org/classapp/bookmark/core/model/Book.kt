package org.classapp.bookmark.core.model

data class Book (
    val id: String? = "",
    val title: String? = "",
    val subTitle: String? = "",
    val description: String? = "",
    val numberOfPage: Int? = 0,
    val isbn: String? = "",
    val authors: String? = "",
    val pubDate: String? = "",
    val genre: String? = ""
) {
    private fun getCoverImageURL(size:String): String {
        return "https://covers.openlibrary.org/b/isbn/${this.isbn}-${size}.jpg"
    }

    public fun getBigCoverImageURL(): String {
        return this.getCoverImageURL("l")
    }
}
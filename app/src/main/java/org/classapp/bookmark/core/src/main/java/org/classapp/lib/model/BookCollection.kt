package org.classapp.bookmark.core.src.main.java.org.classapp.lib.model

data class CollectionEntry (
    val userId: String? = "",
    val bookId: String? = "",
    val pageReaded: Int? = 0,
    val status: String? = ""
)

data class CollectionEntryDetail (
    val user: User,
    val book: Book,
    val entry: CollectionEntry,
    val status: EntryStatus
)

enum class EntryStatus(displayName: String) {
    WANT_TO_READ("Want to read"),
    READING("Reading"),
    COMPLETED("Completed"),
    DROPPED("Dropped")
}
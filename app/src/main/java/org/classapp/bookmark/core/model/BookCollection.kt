package org.classapp.bookmark.core.model

import com.google.firebase.firestore.Exclude

data class CollectionEntry (
    val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val pageReaded: Int? = 0,
    val status: String? = ""
)

data class CollectionEntryDetail (
    val book: Book,
    val entry: CollectionEntry,
    val status: EntryStatus
)

enum class EntryStatus(val displayName: String) {
    WANT_TO_READ("Want to read"),
    READING("Reading"),
    COMPLETED("Completed"),
    DROPPED("Dropped"),
    TBR("To be read")
}

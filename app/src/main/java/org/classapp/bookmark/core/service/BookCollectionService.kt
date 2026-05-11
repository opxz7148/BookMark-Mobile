package org.classapp.bookmark.core.service

import android.security.keystore.UserNotAuthenticatedException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.classapp.bookmark.core.exception.FailedToAddBookToCollectionException
import org.classapp.bookmark.core.exception.FailedToUpdateEntryStatus
import org.classapp.bookmark.core.model.CollectionEntry
import org.classapp.bookmark.core.model.CollectionEntryDetail
import org.classapp.bookmark.core.model.EntryStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCollectionService @Inject constructor(
    private val bookService: BookService,
    private val userService: UserService
){

    private val collectionEntryRef = FirebaseFirestore.getInstance().collection("collectionEntry")

    fun checkUserAuthentication(): Boolean {
        return userService.currentUserId != ""
    }

    suspend fun isBookInUserCollection(bookId: String): Boolean {
        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to check collection")
        }
        val entries = collectionEntryRef
            .whereEqualTo("userId", userService.currentUserId)
            .whereEqualTo("bookId", bookId)
            .get()
            .await()
        return !entries.isEmpty
    }

    suspend fun addBookToCollectionByISBN(isbn: String) {

        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to add book to collection")
        }
        val newBook = bookService.createBookFromISBN(isbn)

        if (isBookInUserCollection(newBook.id)) {
            throw FailedToAddBookToCollectionException("Book with ISBN $isbn is already in user's collection")
        }

        val collectionEntry = CollectionEntry(
            userId = userService.currentUserId,
            bookId = newBook.id,
            pageReaded = 0,
            status = EntryStatus.WANT_TO_READ.name
        )

        collectionEntryRef
            .document()
            .set(collectionEntry)
            .addOnFailureListener { exception -> throw FailedToAddBookToCollectionException("Failed to add book to collection: ${exception.message}") }
            .await()
    }

    suspend fun addBookToCollectionByInput(
        title: String,
        numberOfPage: Int,
        subTitle: String? = "",
        description: String? = "",
        isbn: String? = "",
        authors: List<String>? = emptyList(),
        pubDate: String? = "",
        genre: List<String>? = emptyList(),
        status: String? = EntryStatus.WANT_TO_READ.name
    ) {
        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to add book to collection")
        }
        val book = bookService.createBookFromInput(
            title = title,
            numberOfPage = numberOfPage,
            subTitle = subTitle,
            description = description,
            isbn = isbn,
            authors = authors,
            pubDate = pubDate,
            genre = genre
        )
        val collectionEntry = CollectionEntry(
            userId = userService.currentUserId,
            bookId = book.id,
            pageReaded = 0,
            status = status ?: EntryStatus.WANT_TO_READ.name
        )
        val ref = collectionEntryRef.document()
        ref.set(collectionEntry)
            .addOnFailureListener { throw FailedToAddBookToCollectionException("Failed to add book $title to the collection") }
            .await()
    }

    suspend fun getUserCollectionEntries(): List<CollectionEntryDetail> {
        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to view collection")
        }
        
        val snapshot = collectionEntryRef
            .whereEqualTo("userId", userService.currentUserId)
            .get()
            .await()

        // Manually map document ID to the 'id' field to ensure it's not empty
        val entryList = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CollectionEntry::class.java)?.copy(id = doc.id)
        }

        return getEntryDetail(entryList)
    }

    private suspend fun getEntryDetail(entries: List<CollectionEntry>) : List<CollectionEntryDetail> {

        val bookIdMapEntry = entries.associateBy { it.bookId }
        val bookIds = entries.map { it.bookId }
        val books = bookService.getBooksByIDsFromDB(bookIds)

        val entriesDetails = books.mapNotNull { book ->
            val entry = bookIdMapEntry[book.id]
            entry?.let {
                CollectionEntryDetail(
                    book = book,
                    entry = it,
                    status = EntryStatus.valueOf(it.status ?: EntryStatus.WANT_TO_READ.name)
                )
            }
        }

        return entriesDetails

    }

    suspend fun updateReadingStatus(entryId: String, newStatus: EntryStatus?, pageReaded: Int?) {

        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to change reading status")
        }

        if (entryId.isEmpty()) {
            throw FailedToUpdateEntryStatus("Invalid Entry ID")
        }

        val entryRef = collectionEntryRef.document(entryId)

        val entry = entryRef
            .get()
            .addOnFailureListener { exception -> throw FailedToUpdateEntryStatus("Failed to check entry ownership") }
            .await()

        if (entry.getString("userId") != userService.currentUserId) {
            throw FailedToUpdateEntryStatus("User does not have permission to update this entry")
        }

        val book = bookService.getBookByIDFromDB(entry.getString("bookId")!!)

        val updates = mutableMapOf<String, Any>()

        newStatus?.let { updates["status"] = it.name }
        pageReaded?.let {

            if (it > (book.numberOfPage ?: Int.MAX_VALUE) || it < 0) {
                throw FailedToUpdateEntryStatus("Page readed cannot be greater than the number of pages in the book or less then 0")
            }

            updates["pageReaded"] = it
        }


        entryRef
            .update(updates)
            .addOnFailureListener { exception -> throw FailedToUpdateEntryStatus("Failed to change reading status: ${exception.message}") }
            .await()

    }

    suspend fun removeBookFromCollection(entryId: String) {
        if (!checkUserAuthentication()) {
            throw UserNotAuthenticatedException("User must be authenticated to remove book from collection")
        }

        if (entryId.isEmpty()) {
            throw FailedToUpdateEntryStatus("Invalid Entry ID")
        }

        val entryRef = collectionEntryRef.document(entryId)

        val entry = entryRef
            .get()
            .addOnFailureListener { exception -> throw FailedToUpdateEntryStatus("Failed to check entry ownership") }
            .await()

        if (entry.getString("userId") != userService.currentUserId) {
            throw FailedToUpdateEntryStatus("User does not have permission to remove this entry")
        }

        entryRef
            .delete()
            .addOnFailureListener { exception -> throw FailedToUpdateEntryStatus("Failed to remove book from collection: ${exception.message}") }
            .await()
    }
}
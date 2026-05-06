package org.classapp.bookmark.core.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.classapp.bookmark.core.exception.FailedToAddBookToCollectionException
import org.classapp.bookmark.core.exception.FailedToUpdateEntryStatus
import org.classapp.bookmark.core.model.EntryStatus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log

/**
 * Instrumented test for BookCollectionService.
 * This test interacts with real Firebase services.
 */
@RunWith(AndroidJUnit4::class)
class BookCollectionServiceTest {

    private lateinit var bookCollectionService: BookCollectionService
    private lateinit var bookService: BookService
    private lateinit var userService: UserService
    private val TAG = "BookCollectionServiceTest"
    
    // Tracking lists for cleanup
    private val createdEntryIds = mutableListOf<String>()
    private val createdBookIds = mutableListOf<String>()

    @Before
    fun setUp() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            
            // Initialize Firebase if needed
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            bookService = BookService()
            userService = UserService()
            bookCollectionService = BookCollectionService(bookService, userService)

            // Ensure we have an authenticated user for the tests
            if (!userService.hasUser()) {
                Log.d(TAG, "Signing in anonymously for testing...")
                FirebaseAuth.getInstance().signInAnonymously().await()
            }
            
            createdEntryIds.clear()
            createdBookIds.clear()
        }
    }

    @Test
    fun testAddBookToCollectionByISBN() {
        runBlocking {
            val testIsbn = "9780441172719" // Dune
            
            // 1. Add book by ISBN
            bookCollectionService.addBookToCollectionByISBN(testIsbn)
            
            // 2. Find the book ID to verify and for cleanup
            val book = bookService.getBookByISBNFromDB(testIsbn)
            createdBookIds.add(book.id!!)
            
            // 3. Verify it's in the collection
            val isInCollection = bookCollectionService.isBookInUserCollection(book.id!!)
            assertTrue("Book should be in user collection", isInCollection)
            
            // 4. Verify detail retrieval
            val entries = bookCollectionService.getUserCollectionEntries()
            val entry = entries.find { it.book.isbn == testIsbn }
            
            assertNotNull("Entry should exist in user collection", entry)
            assertEquals("Initial status should be WANT_TO_READ", EntryStatus.WANT_TO_READ, entry?.status)
            
            // Track the entry ID (document ID) for cleanup
            entry?.entry?.id?.let { 
                if (it.isNotEmpty()) createdEntryIds.add(it) 
                else {
                    val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                        .whereEqualTo("userId", userService.currentUserId)
                        .whereEqualTo("bookId", book.id)
                        .get().await()
                    snapshot.documents.firstOrNull()?.id?.let { docId -> createdEntryIds.add(docId) }
                }
            }
        }
    }

    @Test
    fun testAddBookToCollectionByInput() {
        runBlocking {
            val testTitle = "Collection Test Book"
            
            // 1. Add book by manual input
            bookCollectionService.addBookToCollectionByInput(
                title = testTitle,
                numberOfPage = 300,
                status = EntryStatus.READING.name
            )
            
            // 2. Verify it exists in collection entries
            val entries = bookCollectionService.getUserCollectionEntries()
            val entryDetail = entries.find { it.book.title == testTitle }
            
            assertNotNull("Manual entry should exist in collection", entryDetail)
            assertEquals("Status should match input", EntryStatus.READING, entryDetail?.status)
            
            // Track IDs for cleanup
            entryDetail?.book?.id?.let { createdBookIds.add(it) }
            
            // Find the document ID for the entry
            val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                .whereEqualTo("userId", userService.currentUserId)
                .whereEqualTo("bookId", entryDetail?.book?.id)
                .get().await()
            snapshot.documents.firstOrNull()?.id?.let { createdEntryIds.add(it) }
        }
    }

    @Test
    fun testUpdateReadingStatus() {
        runBlocking {
            val testTitle = "Status Update Book"
            
            // 1. Setup: Create an entry
            bookCollectionService.addBookToCollectionByInput(testTitle, 100)
            
            var entries = bookCollectionService.getUserCollectionEntries()
            var entryDetail = entries.find { it.book.title == testTitle }!!
            
            val bookId = entryDetail.book.id
            createdBookIds.add(bookId)
            
            // Find document ID
            val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                .whereEqualTo("userId", userService.currentUserId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            val entryDocId = snapshot.documents.first().id
            createdEntryIds.add(entryDocId)

            // 2. Update status and pages
            val newStatus = EntryStatus.COMPLETED
            val pagesRead = 100
            bookCollectionService.updateReadingStatus(entryDocId, newStatus, pagesRead)
            
            // 3. Verify updates
            entries = bookCollectionService.getUserCollectionEntries()
            entryDetail = entries.find { it.book.id == bookId }!!
            
            assertEquals("Status should be updated to COMPLETED", EntryStatus.COMPLETED, entryDetail.status)
            assertEquals("Pages read should be updated to 100", 100, entryDetail.entry.pageReaded)
        }
    }

    @Test
    fun testRemoveBookFromCollection() {
        runBlocking {
            // 1. Setup: Create an entry
            bookCollectionService.addBookToCollectionByInput("Delete Me", 50)
            
            val entriesBefore = bookCollectionService.getUserCollectionEntries()
            val entryDetail = entriesBefore.find { it.book.title == "Delete Me" }!!
            val bookId = entryDetail.book.id
            createdBookIds.add(bookId)
            
            // Find document ID
            val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                .whereEqualTo("userId", userService.currentUserId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            val entryDocId = snapshot.documents.first().id

            // 2. Remove it
            bookCollectionService.removeBookFromCollection(entryDocId)
            
            // 3. Verify it's gone
            val isInCollection = bookCollectionService.isBookInUserCollection(bookId)
            assertFalse("Book should no longer be in user collection", isInCollection)
        }
    }

    @Test
    fun testAddDuplicateBookToCollection() {
        runBlocking {
            val testIsbn = "9780143111580" // Another test book
            
            // 1. Add book by ISBN first time
            bookCollectionService.addBookToCollectionByISBN(testIsbn)
            
            val book = bookService.getBookByISBNFromDB(testIsbn)
            createdBookIds.add(book.id!!)
            
            // Find entry ID for cleanup
            val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                .whereEqualTo("userId", userService.currentUserId)
                .whereEqualTo("bookId", book.id)
                .get().await()
            snapshot.documents.firstOrNull()?.id?.let { createdEntryIds.add(it) }

            // 2. Try adding same book again
            try {
                bookCollectionService.addBookToCollectionByISBN(testIsbn)
                fail("Expected FailedToAddBookToCollectionException was not thrown for duplicate book")
            } catch (e: FailedToAddBookToCollectionException) {
                assertTrue(e.message?.contains("already in user's collection") == true)
                Log.d(TAG, "✓ Correctly threw FailedToAddBookToCollectionException for duplicate book")
            }
        }
    }

    @Test
    fun testUpdateReadingStatusWithInvalidPageCount() {
        runBlocking {
            val testTitle = "Page Limit Book"
            val totalPages = 100
            
            // 1. Setup: Create an entry for a book with 100 pages
            bookCollectionService.addBookToCollectionByInput(testTitle, totalPages)
            
            val entries = bookCollectionService.getUserCollectionEntries()
            val entryDetail = entries.find { it.book.title == testTitle }!!
            val bookId = entryDetail.book.id
            createdBookIds.add(bookId)
            
            // Find document ID
            val snapshot = FirebaseFirestore.getInstance().collection("collectionEntry")
                .whereEqualTo("userId", userService.currentUserId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            val entryDocId = snapshot.documents.first().id
            createdEntryIds.add(entryDocId)

            // 2. Try to update status with 150 pages (exceeds 100)
            try {
                bookCollectionService.updateReadingStatus(entryDocId, EntryStatus.READING, 150)
                fail("Expected FailedToUpdateEntryStatus was not thrown for exceeding page count")
            } catch (e: FailedToUpdateEntryStatus) {
                assertTrue(e.message?.contains("cannot be greater than the number of pages") == true)
                Log.d(TAG, "✓ Correctly threw FailedToUpdateEntryStatus for exceeding page count")
            }
            
            // 3. Try to update status with negative pages
            try {
                bookCollectionService.updateReadingStatus(entryDocId, EntryStatus.READING, -1)
                fail("Expected FailedToUpdateEntryStatus was not thrown for negative page count")
            } catch (e: FailedToUpdateEntryStatus) {
                assertTrue(e.message?.contains("less then 0") == true)
                Log.d(TAG, "✓ Correctly threw FailedToUpdateEntryStatus for negative page count")
            }
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            val db = FirebaseFirestore.getInstance()
            
            // Cleanup collection entries
            for (id in createdEntryIds) {
                try { 
                    db.collection("collectionEntry").document(id).delete().await() 
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to cleanup entry $id: ${e.message}")
                }
            }
            
            // Cleanup books
            for (id in createdBookIds) {
                try { 
                    db.collection("books").document(id).delete().await() 
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to cleanup book $id: ${e.message}")
                }
            }
            
            Log.d(TAG, "Cleanup finished")
        }
    }
}

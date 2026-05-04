package org.classapp.bookmark.core.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * Unit tests for BookService.
 *
 * These are instrumented tests that run on an Android device/emulator.
 * They test real Firestore database interactions.
 *
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class BookServiceTest {
    private lateinit var context: Context
    private lateinit var bookService: BookService
    private val TAG = "BookServiceTest"

    // Track created book IDs for cleanup
    private val createdBookIds = mutableListOf<String>()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        bookService = BookService()

        // Initialize Firebase if not already done
        val existingApps = FirebaseApp.getApps(context)
        if (existingApps.isEmpty()) {
            FirebaseApp.initializeApp(context)
            Log.d(TAG, "Firebase initialized")
        }

        // Clear tracking list
        createdBookIds.clear()
    }

    /**
     * Test creating a book with input and verify it's saved in Firestore.
     * Verifies that a book can be created with all required fields and queried back.
     */
    @Test
    fun testCreateBookFromInput() {
        runBlocking {
            Log.d(TAG, "Testing book creation from input")

            val book = bookService.createBookFromInput(
                title = "Test Book",
                numberOfPage = 123,
                subTitle = "A Subtitle",
                description = "This is a test book created by unit test.",
                isbn = "1234567890",
                authors = listOf("Author One", "Author Two"),
                pubDate = "2024-01-01",
                genre = listOf("Fiction", "Adventure")
            )

        // Track for cleanup
        createdBookIds.add(book.id!!)

        // Verify book properties
        assertNotNull("Book should not be null", book)
        assertEquals("Title should match", "Test Book", book.title)
        assertEquals("Number of pages should match", 123, book.numberOfPage)
        assertEquals("Subtitle should match", "A Subtitle", book.subTitle)
        assertEquals("ISBN should match", "1234567890", book.isbn)
        assertNotNull("Book ID should not be null", book.id)

        Log.d(TAG, "Book created. Book ID: ${book.id}")

        // Verify book exists in Firestore by ID
        Log.d(TAG, "Querying book from Firestore by ID: ${book.id}")
        val queriedBook = bookService.getBookByIDFromDB(book.id!!)

        assertNotNull("Queried book should not be null", queriedBook)
        assertEquals("Title should match from DB", "Test Book", queriedBook?.title)
        assertEquals("ISBN should match from DB", "1234567890", queriedBook?.isbn)
        assertEquals("Pages should match from DB", 123, queriedBook?.numberOfPage)

        Log.d(TAG, "✓ Book verified in Firestore")
        Log.d(TAG, "Book creation and DB verification test passed")
        }
    }

    /**
     * Test creating a book with minimal fields and verify it's saved in Firestore.
     * Verifies that a book can be created with only required fields and queried back.
     */
    @Test
    fun testCreateBookWithMinimalFields() {
        runBlocking {
        Log.d(TAG, "Testing book creation with minimal fields")

        val book = bookService.createBookFromInput(
            title = "Minimal Book",
            numberOfPage = 50
        )

        // Track for cleanup
        createdBookIds.add(book.id!!)

        // Verify book properties
        assertNotNull("Book should not be null", book)
        assertEquals("Title should match", "Minimal Book", book.title)
        assertEquals("Number of pages should match", 50, book.numberOfPage)
        assertNotNull("Book ID should not be null", book.id)

        Log.d(TAG, "Book created. Book ID: ${book.id}")

        // Verify book exists in Firestore
        Log.d(TAG, "Querying book from Firestore by ID: ${book.id}")
        val queriedBook = bookService.getBookByIDFromDB(book.id!!)

        assertNotNull("Queried book should not be null", queriedBook)
        assertEquals("Title should match from DB", "Minimal Book", queriedBook?.title)
        assertEquals("Pages should match from DB", 50, queriedBook?.numberOfPage)

        Log.d(TAG, "✓ Minimal book verified in Firestore")
        Log.d(TAG, "Minimal book creation and DB verification test passed")
        }
    }

    /**
     * Test creating a book with optional fields as lists and verify it's saved in Firestore.
     * Verifies that authors and genres can be passed as lists and are persisted correctly.
     */
    @Test
    fun testCreateBookWithListFields() {
        runBlocking {
        Log.d(TAG, "Testing book creation with list fields")

        val book = bookService.createBookFromInput(
            title = "Multi-Author Book",
            numberOfPage = 200,
            authors = listOf("Author One", "Author Two", "Author Three"),
            genre = listOf("Science Fiction", "Adventure", "Mystery")
        )

        // Track for cleanup
        createdBookIds.add(book.id!!)

        // Verify book properties
        assertNotNull("Book should not be null", book)
        assertEquals("Title should match", "Multi-Author Book", book.title)
        assertNotNull("Authors should not be null", book.authors)
        assertNotNull("Book ID should not be null", book.id)

        Log.d(TAG, "Book created. Book ID: ${book.id}")

        // Verify book exists in Firestore
        Log.d(TAG, "Querying book from Firestore by ID: ${book.id}")
        val queriedBook = bookService.getBookByIDFromDB(book.id!!)

        assertNotNull("Queried book should not be null", queriedBook)
        assertEquals("Title should match from DB", "Multi-Author Book", queriedBook?.title)
        assertEquals("Number of pages should match from DB", 200, queriedBook?.numberOfPage)
        assertNotNull("Authors should not be null in DB", queriedBook?.authors)
        assertNotNull("Genres should not be null in DB", queriedBook?.genre)

        Log.d(TAG, "✓ Multi-author book verified in Firestore")
        Log.d(TAG, "List fields book creation and DB verification test passed")
        }
    }

    /**
     * Test that created books have valid IDs and are saved in Firestore.
     * Verifies that Firestore generates valid unique document IDs.
     */
    @Test
    fun testBookIdGeneration() {
        runBlocking {
        Log.d(TAG, "Testing book ID generation")

        val book1 = bookService.createBookFromInput(
            title = "Book One",
            numberOfPage = 100
        )

        val book2 = bookService.createBookFromInput(
            title = "Book Two",
            numberOfPage = 150
        )

        // Track for cleanup
        createdBookIds.add(book1.id!!)
        createdBookIds.add(book2.id!!)

        // Verify IDs are not empty and unique
        assertNotNull("Book 1 ID should not be null", book1.id)
        assertNotNull("Book 2 ID should not be null", book2.id)
        assertNotEquals("Book IDs should be unique", book1.id, book2.id)

        Log.d(TAG, "Book 1 ID: ${book1.id}")
        Log.d(TAG, "Book 2 ID: ${book2.id}")

        // Verify both books exist in Firestore
        Log.d(TAG, "Querying both books from Firestore")
        val queriedBook1 = bookService.getBookByIDFromDB(book1.id!!)
        val queriedBook2 = bookService.getBookByIDFromDB(book2.id!!)

        assertNotNull("Book 1 should be found in DB", queriedBook1)
        assertNotNull("Book 2 should be found in DB", queriedBook2)
        assertEquals("Book 1 title should match from DB", "Book One", queriedBook1?.title)
        assertEquals("Book 2 title should match from DB", "Book Two", queriedBook2?.title)

        Log.d(TAG, "✓ Both books verified in Firestore")
        Log.d(TAG, "Book ID generation and DB verification test passed")
        }
    }

    /**
     * Test creating book from ISBN and verify it's saved in Firestore.
     * Verifies that a book can be created by providing an ISBN and is persisted.
     */
    @Test
    fun testCreateBookFromISBN() {
        runBlocking {
        Log.d(TAG, "Testing book creation from ISBN")

        val isbn = "9780441172719"
        val book = bookService.createBookFromISBN(isbn)

        // Track for cleanup
        createdBookIds.add(book.id!!)

        // Verify book properties
        assertNotNull("Book should not be null", book)
        assertEquals("ISBN should match", isbn, book.isbn)
        assertNotNull("Book ID should not be null", book.id)

        Log.d(TAG, "Book created from ISBN. Book ID: ${book.id}")

        // Verify book exists in Firestore by ISBN
        Log.d(TAG, "Querying book from Firestore by ISBN: $isbn")
        val queriedBook = bookService.getBookByISBNFromDB(isbn)

        assertNotNull("Queried book should not be null", queriedBook)
        assertEquals("ISBN should match from DB", isbn, queriedBook?.isbn)

        Log.d(TAG, "✓ Book verified in Firestore")
        Log.d(TAG, "Book creation from ISBN and DB verification test passed")
        }
    }

    /**
     * Test that book details are persisted in Firestore correctly.
     * Verifies that all book details are properly saved and can be retrieved.
     */
    @Test
    fun testBookDetailsPersistence() {
        runBlocking {
        Log.d(TAG, "Testing book details persistence")

        val testTitle = "Persistence Test Book"
        val testPages = 256
        val testSubtitle = "Testing Details"
        val testISBN = "1111111111"
        val testDescription = "A test description for persistence"

        val book = bookService.createBookFromInput(
            title = testTitle,
            numberOfPage = testPages,
            subTitle = testSubtitle,
            isbn = testISBN,
            description = testDescription
        )

        // Track for cleanup
        createdBookIds.add(book.id!!)

        // Verify all details are correctly set in memory
        assertEquals("Title should persist in memory", testTitle, book.title)
        assertEquals("Page count should persist in memory", testPages, book.numberOfPage)
        assertEquals("Subtitle should persist in memory", testSubtitle, book.subTitle)
        assertEquals("ISBN should persist in memory", testISBN, book.isbn)
        assertEquals("Description should persist in memory", testDescription, book.description)

        Log.d(TAG, "Book created. Book ID: ${book.id}")

        // Verify all details are persisted in Firestore
        Log.d(TAG, "Querying book from Firestore to verify persistence")
        val queriedBook = bookService.getBookByIDFromDB(book.id!!)

        assertNotNull("Queried book should not be null", queriedBook)
        assertEquals("Title should persist in DB", testTitle, queriedBook?.title)
        assertEquals("Page count should persist in DB", testPages, queriedBook?.numberOfPage)
        assertEquals("Subtitle should persist in DB", testSubtitle, queriedBook?.subTitle)
        assertEquals("ISBN should persist in DB", testISBN, queriedBook?.isbn)
        assertEquals("Description should persist in DB", testDescription, queriedBook?.description)

        Log.d(TAG, "✓ All book details verified in Firestore")
        Log.d(TAG, "Book details persistence test passed")
        }
    }

    /**
     * Test creating book from invalid ISBN and verify it throws an exception.
     * Verifies that attempting to create a book with an invalid/non-existent ISBN fails gracefully.
     */
    @Test
    fun testCreateBookFromInvalidISBN() {
        runBlocking {
            Log.d(TAG, "Testing book creation from invalid ISBN")

            val invalidISBN = "9999999999999"
            Log.d(TAG, "Attempting to create book with invalid ISBN: $invalidISBN")

            try {
                val book = bookService.createBookFromISBN(invalidISBN)
                Log.e(TAG, "❌ Test failed: Expected exception but book was created: $book")
                fail("Expected BookNotFoundException for invalid ISBN but no exception was thrown")
            } catch (e: Exception) {
                Log.d(TAG, "✓ Expected exception caught: ${e::class.simpleName}")
                Log.d(TAG, "✓ Exception message: ${e.message}")

                // Verify it's the expected exception type
                assertTrue(
                    "Expected BookNotFoundException but got ${e::class.simpleName}",
                    e.message?.contains("not found") == true ||
                    e::class.simpleName == "BookNotFoundException"
                )
                Log.d(TAG, "✓ Invalid ISBN test passed - correctly threw exception")
            }
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            // Clean up: Delete all books created during tests
            if (createdBookIds.isNotEmpty()) {
                Log.d(TAG, "Cleaning up ${createdBookIds.size} test books from Firestore")

                val firestore = FirebaseFirestore.getInstance()
                val booksCollection = firestore.collection("books")

                for (bookId in createdBookIds) {
                    try {
                        booksCollection.document(bookId).delete().await()
                        Log.d(TAG, "✓ Deleted test book: $bookId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete test book $bookId: ${e.message}", e)
                    }
                }

                Log.d(TAG, "Test cleanup completed. All ${createdBookIds.size} test books deleted.")
                createdBookIds.clear()
            } else {
                Log.d(TAG, "No test books to clean up")
            }
        }
    }
}













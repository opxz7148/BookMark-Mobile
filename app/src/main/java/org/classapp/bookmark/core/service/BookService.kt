package org.classapp.bookmark.core.service

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.classapp.bookmark.core.exception.BookCreateFailedException
import org.classapp.bookmark.core.exception.BookNotFoundException
import org.classapp.bookmark.core.exception.FailedToFetchBook
import org.classapp.bookmark.core.model.Book
import org.classapp.bookmark.core.service.booksoure.GoogleBookAPIInstance
import javax.inject.Inject

class BookService  @Inject constructor() {

    private val TAG = "BookService"

    // Firestore book collection reference (Placeholder)
    private val bookCollectionRef = FirebaseFirestore.getInstance().collection("books")

    // Get new book from ISBN (query from some API)
    public suspend fun getBookByISBN(isbn: String): Book {
        // Placeholder implementation
        val id = GoogleBookAPIInstance.fetchBookFromISBN(isbn).getFirstID()?: throw BookNotFoundException("Book with ISBN $isbn not found")
        val bookDetail = GoogleBookAPIInstance.fetchBookDetailById(id)
        return bookDetail.toBook()
    }

    // Create new book from user input
    public fun createBookFromInput(
        title: String,
        numberOfPage: Int,
        subTitle: String? = "",
        description: String? = "",
        isbn: String? = "",
        authors: List<String>? = emptyList(),
        pubDate: String? = "",
        genre: List<String>? = emptyList()
    ): Book {
        // Placeholder implementation

        val ref = bookCollectionRef.document() // Generate new document reference

        val book: Book = Book(
            id = ref.id, // Use generated document ID as book ID
            title = title,
            subTitle = subTitle,
            description = description,
            numberOfPage = numberOfPage,
            isbn = isbn,
            pubDate = pubDate,
            authors = authors,
            genre = genre
        )

        ref.set(book) // Save book to Firestore
         .addOnFailureListener { e ->
            throw BookCreateFailedException("Failed to create book: ${e.message}")
        }
        return book
    }

    public suspend fun createBookFromISBN(isbn: String): Book {
        val book = GoogleBookAPIInstance.fetchBookFromISBN(isbn)
            .getFirstID()
            ?.let {
                id -> GoogleBookAPIInstance.fetchBookDetailById(id).toBook()
        } ?: throw BookNotFoundException("Book with ISBN $isbn not found")

        bookCollectionRef
            .document(book.id)
            .set(book)
            .addOnFailureListener { e ->
                throw BookCreateFailedException("Failed to create book from ISBN: ${e.message}")
            }

        return book
    }

    // Get book by ISBN (Search from DB)
    public suspend fun getBookByISBNFromDB(isbn: String): Book {
        val book = bookCollectionRef
            .whereEqualTo("isbn", isbn)
            .get()
            .addOnFailureListener { exception -> throw FailedToFetchBook("Failed to fetch book from database by ISBN: ${exception.message}") }
            .await()
            .toObjects(Book::class.java)
            .firstOrNull()
        return book ?: throw BookNotFoundException("Book with ISBN $isbn not found in database")
    }

    // Get book by ID (Search from DB)
    public suspend fun getBookByIDFromDB(id: String): Book? {
        val book = bookCollectionRef
            .document(id)
            .get()
            .addOnFailureListener { exception -> throw FailedToFetchBook("Failed to fetch book from database by ID: ${exception.message}") }
            .await()
            .toObject(Book::class.java)
        return book ?: throw BookNotFoundException("Book with ID $id not found in database")
    }

    // Get batch of books by IDs (Search from DB)
    public fun getBooksByIDsFromDB(ids: List<String>): List<Book> {
        throw NotImplementedError("Batch fetching by IDs from database is not implemented yet")
    }

    // Get batch of books by ISBNs (Search from DB)
    public fun getBooksByISBNsFromDB(isbns: List<String>): List<Book> {
        throw NotImplementedError("Batch fetching by ISBNs from database is not implemented yet")
    }

}
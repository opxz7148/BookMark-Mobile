package org.classapp.bookmark.core.service

import com.google.firebase.firestore.FirebaseFirestore
import org.classapp.bookmark.core.exception.BookCreateFailedException
import org.classapp.bookmark.core.model.Book
import javax.inject.Inject

class BookService  @Inject constructor() {

    // Firestore book collection reference (Placeholder)
    private val bookCollectionRef = FirebaseFirestore.getInstance().collection("books")

    // Get new book from ISBN (query from some API)
    public fun getBookByISBN(isbn: String): Book {
        // Placeholder implementation
        return Book(
            title = "Example Book",
            subTitle = "An Example Subtitle",
            description = "This is an example book description.",
            numberOfPage = 300,
            isbn = isbn,
            authors = "John Doe",
            pubDate = "2024-01-01"
        )
    }

    // Create new book from user input
    public fun createBookFromInput(
         title: String,
         numberOfPage: Int,
         subTitle: String? = "",
         description: String? = "",
         isbn: String? = "",
         authors: String? = "",
         pubDate: String? = "",
         genre: String? = ""
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
            authors = authors,
            pubDate = pubDate,
            genre = genre
        )

        ref.set(book) // Save book to Firestore
         .addOnFailureListener { e ->
            throw BookCreateFailedException("Failed to create book: ${e.message}")
        }
        return book
    }

    public fun createBookFromISBN(isbn: String): Book {
        // Placeholder implementation
        val book = getBookByISBN(isbn)
        return createBookFromInput(
            title = book.title.orEmpty(),
            numberOfPage = book.numberOfPage ?: 0,
            subTitle = book.subTitle.orEmpty(),
            description = book.description.orEmpty(),
            isbn = book.isbn.orEmpty(),
            authors = book.authors.orEmpty(),
            pubDate = book.pubDate.orEmpty(),
            genre = book.genre.orEmpty()
        )
    }

    // Get book by ISBN (Search from DB)
    public fun getBookByISBNFromDB(isbn: String): Book? {
        // Placeholder implementation
        return null
    }

    // Get book by ID (Search from DB)
    public fun getBookByIDFromDB(id: String): Book? {
        // Placeholder implementation
        return null
    }

    // Get batch of books by IDs (Search from DB)
    public fun getBooksByIDsFromDB(ids: List<String>): List<Book> {
        // Placeholder implementation
        return emptyList()
    }

    // Get batch of books by ISBNs (Search from DB)
    public fun getBooksByISBNsFromDB(isbns: List<String>): List<Book> {
        // Placeholder implementation
        return emptyList()
    }

}
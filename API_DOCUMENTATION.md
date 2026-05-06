# BookMark API Documentation

This document provides an overview of the core services and data models used in the BookMark application.

---

## 🏗️ Core Services

### 1. `UserService`
Manages user authentication and account lifecycle using Firebase Auth.

| Method | Description |
| :--- | :--- |
| `currentUser: Flow<User?>` | Flow that emits the current user state whenever it changes. |
| `currentUserId: String` | Returns the UID of the currently logged-in user. |
| `hasUser(): Boolean` | Checks if a user is currently signed in. |
| `signIn(email, password)` | Authenticates a user with email and password. |
| `signUp(email, password, displayName)` | Creates a new user account and updates their profile. |
| `signOut()` | Signs out the current user. |
| `deleteAccount()` | Deletes the currently authenticated user's account. |

### 2. `BookService`
Handles book-related operations, including fetching details from external APIs (Google Books) and persisting book data in Firestore.

| Method | Description |
| :--- | :--- |
| `getBookByISBN(isbn)` | Fetches book details from the Google Books API using an ISBN. |
| `createBookFromISBN(isbn)` | Lookups a book by ISBN; if not in DB, fetches from API and saves to Firestore. |
| `createBookFromInput(...)` | Manually creates and saves a book record in Firestore. |
| `getBookByISBNFromDB(isbn)` | Retrieves a book record from Firestore based on its ISBN. |
| `getBookByIDFromDB(id)` | Retrieves a book record from Firestore based on its unique Document ID. |
| `getBooksByIDsFromDB(ids)` | Batch fetches multiple books from Firestore using a list of IDs. |
| `isBookInDBByISBN(isbn)` | Checks if a book with the given ISBN already exists in the Firestore database. |

### 3. `BookCollectionService`
Manages the user's personal book collection, linking users to books with reading progress.

| Method | Description |
| :--- | :--- |
| `getUserCollectionEntries()` | Retrieves all books in the current user's collection with their status and progress. |
| `addBookToCollectionByISBN(isbn)` | Adds a book to the user's collection using its ISBN. |
| `addBookToCollectionByInput(...)` | Adds a manually entered book to the user's collection. |
| `updateReadingStatus(entryId, status, pages)` | Updates the reading status or page count for a specific collection entry. |
| `removeBookFromCollection(entryId)` | Deletes a book from the user's collection. |
| `isBookInUserCollection(bookId)` | Checks if a specific book ID is already present in the user's collection. |

---

## 📦 Data Models

### `User`
Represents an application user.
- `id`: Unique identifier (UID from Firebase).
- `username`: Display name.
- `email`: User's email address.

### `Book`
Represents a book entity in the system.
- `id`: Firestore Document ID.
- `title`: Primary title of the book.
- `subTitle`: Optional subtitle.
- `isbn`: 10 or 13-digit ISBN.
- `numberOfPage`: Total page count.
- `authors`: List of author names.
- `genre`: List of categories/genres.
- `coverImageUrl`: List of URLs for book covers.

### `CollectionEntry`
Links a user to a book in their collection.
- `id`: Document ID in the `collectionEntry` collection.
- `userId`: ID of the user who owns this entry.
- `bookId`: ID of the book being tracked.
- `pageReaded`: Number of pages the user has completed.
- `status`: String representation of `EntryStatus`.

### `CollectionEntryDetail`
A UI-friendly wrapper that combines a `Book` and its corresponding `CollectionEntry`.
- `book`: The full book details.
- `entry`: The raw collection metadata.
- `status`: The parsed `EntryStatus` enum.

### `EntryStatus` (Enum)
Represents the current state of a book in a user's collection.
- `WANT_TO_READ`
- `READING`
- `COMPLETED`
- `DROPPED`
- `TBR` (To be read)

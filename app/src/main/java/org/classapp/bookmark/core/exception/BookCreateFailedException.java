package org.classapp.bookmark.core.exception;

public class BookCreateFailedException extends RuntimeException {
    public BookCreateFailedException(String message) {
        super(message);
    }
}

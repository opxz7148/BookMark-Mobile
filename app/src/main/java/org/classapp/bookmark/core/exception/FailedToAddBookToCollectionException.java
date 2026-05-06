package org.classapp.bookmark.core.exception;

public class FailedToAddBookToCollectionException extends RuntimeException {
    public FailedToAddBookToCollectionException(String message) {
        super(message);
    }
}

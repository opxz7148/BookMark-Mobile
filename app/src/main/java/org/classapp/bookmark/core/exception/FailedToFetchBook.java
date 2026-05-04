package org.classapp.bookmark.core.exception;

public class FailedToFetchBook extends RuntimeException {
    public FailedToFetchBook(String message) {
        super(message);
    }
}

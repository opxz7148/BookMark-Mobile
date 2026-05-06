package org.classapp.bookmark.core.exception;

public class FailedToUpdateEntryStatus extends RuntimeException {
    public FailedToUpdateEntryStatus(String message) {
        super(message);
    }
}

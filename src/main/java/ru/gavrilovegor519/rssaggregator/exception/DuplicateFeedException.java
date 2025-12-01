package ru.gavrilovegor519.rssaggregator.exception;

public class DuplicateFeedException extends RuntimeException {
    public DuplicateFeedException(String message) {
        super(message);
    }
}

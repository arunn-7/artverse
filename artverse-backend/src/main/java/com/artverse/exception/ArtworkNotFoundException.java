package com.artverse.exception;

public class ArtworkNotFoundException extends RuntimeException {

    public ArtworkNotFoundException(String message) {
        super(message);
    }
}
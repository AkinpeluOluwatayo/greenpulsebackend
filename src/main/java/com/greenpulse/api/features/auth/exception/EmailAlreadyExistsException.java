package com.greenpulse.api.features.auth.exception;

public class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " is already registered.");
    }
}

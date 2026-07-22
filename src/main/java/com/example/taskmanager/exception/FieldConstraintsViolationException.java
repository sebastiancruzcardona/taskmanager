package com.example.taskmanager.exception;

public class FieldConstraintsViolationException extends RuntimeException {
    public FieldConstraintsViolationException(String message) {
        super(message);
    }
}

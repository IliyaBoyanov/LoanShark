package com.example.loanShark.exceptions;

public class IdempotentKeyNotUnique extends RuntimeException {
    String errorCode;

    public IdempotentKeyNotUnique(String message) {
        super(message);
    }

    public IdempotentKeyNotUnique(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

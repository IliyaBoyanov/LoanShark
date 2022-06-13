package com.example.loanShark.exceptions;

public class PaymentAlreadyUpdated extends RuntimeException {
    String errorCode;

    public PaymentAlreadyUpdated(String message) {
        super(message);
    }

    public PaymentAlreadyUpdated(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

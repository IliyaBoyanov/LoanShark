package com.example.loanShark.exceptions;

public class LoanAlreadyPaidException extends RuntimeException {
    String errorCode;

    public LoanAlreadyPaidException(String msg) {
        super(msg);
    }

    public LoanAlreadyPaidException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public LoanAlreadyPaidException with(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}

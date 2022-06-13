package com.example.loanShark.dtos;


public class ErrorResponse {
    public String message;
    public String errorCode;

    public ErrorResponse from(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.message = e.getMessage();
        return errorResponse;
    }

    public ErrorResponse with(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}

package com.example.loanShark.exceptions;

import com.example.loanShark.dtos.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LoanAlreadyPaidException.class)
    public final ResponseEntity<ErrorResponse> handleLoanPaymentException(LoanAlreadyPaidException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e).with(e.errorCode), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public final ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public final ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentAlreadyUpdated.class)
    public final ResponseEntity<ErrorResponse> handleUsernameNotFoundException(PaymentAlreadyUpdated e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e).with(e.errorCode), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdempotentKeyNotUnique.class)
    public final ResponseEntity<ErrorResponse> handleIdempotentKeyExistException(IdempotentKeyNotUnique e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e).with(e.errorCode), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().from(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
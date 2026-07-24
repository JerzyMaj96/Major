package com.jerzymaj.major.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {

        return buildResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDetails> buildResponse(Exception ex, WebRequest request, HttpStatus status) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {

        return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExistingUserException.class)
    public final ResponseEntity<ErrorDetails> handleExistingUserException(ExistingUserException ex, WebRequest request) {

        return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
    }
}

package com.kalebot.controller;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(WebExchangeBindException.class)
  public ProblemDetail handleValidation(WebExchangeBindException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Validation failed");
    detail.setDetail("One or more fields are invalid.");
    List<String> errors = ex.getFieldErrors().stream()
        .map(error -> error.getField() + ": "
            + Optional.ofNullable(error.getDefaultMessage()).orElse("invalid"))
        .toList();
    detail.setProperty("errors", errors);
    return detail;
  }

  @ExceptionHandler(ServerWebInputException.class)
  public ProblemDetail handleInput(ServerWebInputException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Invalid request");
    detail.setDetail(Optional.ofNullable(ex.getReason()).orElse("Malformed request body."));
    return detail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Invalid request");
    detail.setDetail(ex.getMessage());
    return detail;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalState(IllegalStateException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("Request cannot be processed");
    detail.setDetail(ex.getMessage());
    return detail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    logger.error("Unhandled exception", ex);
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("Server error");
    detail.setDetail("Unexpected error.");
    return detail;
  }
}

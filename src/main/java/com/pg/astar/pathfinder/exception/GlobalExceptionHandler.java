package com.pg.astar.pathfinder.exception;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the application. Processes exceptions and returns appropriate HTTP
 * responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(NodeNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNodeNotFoundException(
      NodeNotFoundException ex, WebRequest request) {
    log.error("Node not found exception: {}", ex.getMessage());
    ErrorResponse errorResponse =
        buildErrorResponse(HttpStatus.NOT_FOUND, "Node Not Found", ex.getMessage(), request);
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequestException(Exception ex, WebRequest request) {
    log.error("Bad request exception: {}", ex.getMessage());
    ErrorResponse errorResponse =
        buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoPathFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoPathFoundException(
      NoPathFoundException ex, WebRequest request) {
    log.error("No path found exception: {}", ex.getMessage());
    ErrorResponse errorResponse =
        buildErrorResponse(HttpStatus.NOT_FOUND, "Path Not Found", ex.getMessage(), request);
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
    log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse =
        buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Builds a consistent error response */
  private ErrorResponse buildErrorResponse(
      HttpStatus status, String error, String message, WebRequest request) {
    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status.value())
        .error(error)
        .message(message)
        .path(request.getDescription(false).substring(4))
        .build();
  }
}

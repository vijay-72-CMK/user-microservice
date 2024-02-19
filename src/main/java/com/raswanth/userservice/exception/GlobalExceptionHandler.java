package com.raswanth.userservice.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }
    @ExceptionHandler({DataAccessException.class})
    public ProblemDetail handleDataAccessException(DataAccessException exception) {
        logger.error("YOOO", exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "something went wrong");
        problemDetail.setTitle("Some field is duplicated");
        problemDetail.setType(URI.create("http://localhost:8080/errors/duplicateFeilds"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Some field is duplicated");
        problemDetail.setType(URI.create("http://localhost:8080/errors/duplicateFeilds"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        // Create the list of invalid parameters
        List<Map<String, String>> invalidParams = fieldErrors.stream()
                .map(fieldError -> {
                    Map<String, String> invalidParam = new LinkedHashMap<>();
                    invalidParam.put("field", fieldError.getField());
                    invalidParam.put("reason", fieldError.getDefaultMessage());
                    return invalidParam;
                })
                .collect(Collectors.toList());

        // Create the ProblemDetail object
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Please correct invalid params shown below");
        problemDetail.setTitle("Your request parameters didn't validate.");
        problemDetail.setType(URI.create("http://localhost:8080/errors/invalidParameters"));
        problemDetail.setProperty("invalid-params", invalidParams);
        problemDetail.setProperty("timestamp", Instant.now());

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }


}

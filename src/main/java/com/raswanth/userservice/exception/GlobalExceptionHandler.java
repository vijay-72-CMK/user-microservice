//package com.raswanth.userservice.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ProblemDetail;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.net.URI;
//import java.time.Instant;
//
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler extends BaseExceptionHandler {
//    protected GlobalExceptionHandler() {
//        super(log);
//    }
//
//    @ExceptionHandler(UserAlreadyExistsException.class)
//    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
//        problemDetail.setTitle("Some field is duplicated");
//        problemDetail.setType(URI.create("http://localhost:8080/errors/duplicateFeilds"));
//        problemDetail.setProperty("timestamp", Instant.now());
//        return problemDetail;
//    }
//}

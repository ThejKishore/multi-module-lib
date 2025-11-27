package com.tk.learn.web;

import com.tk.learn.model.exceptions.InValidObjectException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InValidObjectException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest(InValidObjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getErrors());
    }
}

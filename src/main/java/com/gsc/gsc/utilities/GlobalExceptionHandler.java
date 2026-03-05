package com.gsc.gsc.utilities;

import com.gsc.gsc.constants.ReturnObject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ReturnObject> handleExpiredJwt(ExpiredJwtException ex) {

        ReturnObject response = new ReturnObject(
                "JWT token expired",
                null,
                0,
                false
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ReturnObject> handleJwt(JwtException ex) {

        ReturnObject response = new ReturnObject(
                "Invalid JWT token",
                null,
                0,
                false
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}
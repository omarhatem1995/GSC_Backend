package com.gsc.gsc.utilities;

import com.gsc.gsc.constants.ReturnObject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ReturnObject> handleExpiredJwt(ExpiredJwtException ex,
                                                         HttpServletRequest request) {
        log.warn("[AUTH ERROR] Expired JWT | {} {} | {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ReturnObject("JWT token expired", null, 0, false));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ReturnObject> handleJwt(JwtException ex,
                                                   HttpServletRequest request) {
        log.warn("[AUTH ERROR] Invalid JWT | {} {} | {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ReturnObject("Invalid JWT token", null, 0, false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReturnObject> handleGeneral(Exception ex,
                                                      HttpServletRequest request) {
        log.error("[UNHANDLED ERROR] {} {} | {} : {}",
                request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ReturnObject("An unexpected error occurred", null, 0, false));
    }
}
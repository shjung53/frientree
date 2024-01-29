package com.d101.frientree.exception;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Gson gson;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(msg);
    }

    @ExceptionHandler(PasswordNotMatchingException.class)
    public ResponseEntity<String> handlePasswordNotMatchingException(PasswordNotMatchingException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(msg);
    }

    @ExceptionHandler(EmailDuplicatedException.class)
    public ResponseEntity<String> handleEmailDuplicatedException(EmailDuplicatedException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(msg);
    }

    @ExceptionHandler(NicknameValidateException.class)
    public ResponseEntity<String> handleNicknameValidateException(NicknameValidateException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(msg);
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<String> handleJwtValidationException(JwtValidationException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(msg);
    }

    @ExceptionHandler(EmailCodeNotMatchingException.class)
    public ResponseEntity<String> handleEmailCodeNotMatchingException(EmailCodeNotMatchingException e) {
        String msg = gson.toJson(Collections.singletonMap("message", e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(msg);
    }

}

package com.example.installmentreminder.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Validation failure: {}", ex.getMessage());
		return ResponseEntity.badRequest().body(ApiResponse.ok("Validation failed", Map.of("error", ex.getMessage())));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
		log.warn("Request body validation failed. fieldErrorCount={}", ex.getBindingResult().getFieldErrorCount());
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.ok("Validation failed", errors));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodValidation(
			HandlerMethodValidationException ex) {
		log.warn("Method parameter validation failed: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.ok("Validation failed", Map.of("error", ex.getMessage())));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleGeneral(Exception ex) {
		log.error("Unexpected unhandled exception", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.ok("Unexpected error", Map.of("error", ex.getMessage())));
	}
}

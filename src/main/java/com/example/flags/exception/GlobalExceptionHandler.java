package com.example.flags.exception;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<Map<String,String>> notFound(NotFoundException e){ return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<Map<String,String>> bad(BadRequestException e){ return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
  @ExceptionHandler(RateLimitExceededException.class)
  ResponseEntity<Map<String,String>> limited(RateLimitExceededException e){ return ResponseEntity.status(429).body(Map.of("error", e.getMessage())); }
  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String,String>> generic(Exception e){ return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", e.getMessage())); }
}

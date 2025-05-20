package com.zzimple.global.exception;

import com.zzimple.global.common.response.ApiResponse;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
    log.error("Custom 오류 발생: {}", ex.getMessage());
    return ResponseEntity
        .status(ex.getErrorCode().getStatus())
        .body(ApiResponse.failure(
            ex.getErrorCode().getCode(),       // 예: "E001"
            ex.getErrorCode().getMessage()     // 예: "임시 주소 저장 실패"
        ));
  }

  // Validation 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" / "));

    log.warn("Validation 오류 발생: {}", errorMessages);

    return ResponseEntity
        .badRequest()
        .body(ApiResponse.failure("VALIDATION_ERROR", errorMessages));
  }

  // 예상치 못한 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>>  handleException(Exception ex) {
    log.error("Server 오류 발생: ", ex);

    return ResponseEntity
        .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ApiResponse.failure(
            GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        ));
  }
}
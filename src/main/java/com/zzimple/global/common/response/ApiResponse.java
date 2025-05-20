package com.zzimple.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private boolean success;
  private String code;
  private String message;
  private T data;

  // 성공 응답
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .build();
  }

  // 실패 응답
  public static <T> ApiResponse<T> failure(String code, String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .code(code)
        .message(message)
        .data(null)
        .build();
  }
}
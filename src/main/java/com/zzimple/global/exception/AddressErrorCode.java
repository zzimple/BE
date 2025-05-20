package com.zzimple.global.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum AddressErrorCode implements BaseErrorCode {

  ADDRESS_DRAFT_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "임시 주소 저장 실패");

  private final HttpStatus status;
  private final String code;
  private final String message;
}

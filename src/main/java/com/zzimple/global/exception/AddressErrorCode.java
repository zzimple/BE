package com.zzimple.global.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum AddressErrorCode implements BaseErrorCode {

  ADDRESS_DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "임시 주소 정보가 존재하지 않습니다."),
  JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "Redis에 저장된 주소 정보를 파싱하는 데 실패했습니다."),
  ADDRESS_DRAFT_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "A003", "임시 주소 저장 실패");

  private final HttpStatus status;
  private final String code;
  private final String message;
}

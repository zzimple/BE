package com.zzimple.owner.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode implements BaseErrorCode {

  BUSINESS_NUMBER_MISSING("B4041", "사업자등록번호가 누락되었습니다.", HttpStatus.BAD_REQUEST),
  BUSINESS_NUMBER_INVALID("B4042", "유효하지 않은 사업자등록번호입니다.", HttpStatus.BAD_REQUEST),

  BUSINESS_NUMBER_ALREADY_EXISTS("B4091", "이미 존재하는 사업자등록번호입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

package com.zzimple.estimate.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HolidayErrorCode implements BaseErrorCode {

  API_CALL_FAILED("H001", "공휴일 API 호출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  HOLIDAY_DRAFT_NOT_FOUND("H002", "저장된 공휴일 정보가 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}

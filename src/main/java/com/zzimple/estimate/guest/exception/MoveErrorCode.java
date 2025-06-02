package com.zzimple.estimate.guest.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MoveErrorCode implements BaseErrorCode {

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E001", "요청 값이 유효하지 않습니다."),
  MOVE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "저장된 이사 유형이 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}

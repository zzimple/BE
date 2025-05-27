package com.zzimple.staff.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StaffErrorCode implements BaseErrorCode {

  INVALID_STAFF_ROLE("ST001", "직원 권한이 아닙니다.", HttpStatus.FORBIDDEN),
  OWNER_NOT_FOUND("ST002", "사장님을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_OWNER_ROLE("ST003", "사장님 권한이 아닙니다.", HttpStatus.FORBIDDEN),
  APPROVAL_ALREADY_REQUESTED("ST004", "이미 승인 요청이 존재합니다.", HttpStatus.CONFLICT),
  STAFF_OWNER_MISMATCH("ST005", "요청자와 직원 소속 사장님이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

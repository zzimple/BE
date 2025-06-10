package com.zzimple.staff.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StaffErrorCode implements BaseErrorCode {

  INVALID_STAFF_ROLE("ST4041", "직원 권한이 아닙니다.", HttpStatus.FORBIDDEN),
  OWNER_NOT_FOUND("ST4042", "사장님을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_OWNER_ROLE("ST4043", "사장님 권한이 아닙니다.", HttpStatus.FORBIDDEN),
  APPROVAL_ALREADY_REQUESTED("ST4091", "이미 승인 요청이 존재합니다.", HttpStatus.CONFLICT),
  STAFF_OWNER_MISMATCH("ST4011", "요청자와 직원 소속 사장님이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_STORE_ASSIGNMENT("ST4044", "직원이 이 매장에 속해있지 않습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

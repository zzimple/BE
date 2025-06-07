package com.zzimple.owner.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OwnerErrorCode implements BaseErrorCode {

  OWNER_NOT_FOUND("O4041", "사장님 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  STORE_NOT_FOUND("U4042", "매장 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}

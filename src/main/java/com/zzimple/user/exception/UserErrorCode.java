package com.zzimple.user.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

  // 로그인 실패
  LOGIN_ID_NOT_FOUND("U001", "존재하지 않는 아이디입니다.", HttpStatus.UNAUTHORIZED),
  INVALID_PASSWORD("U002", "비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),

  // 회원가입 실패
  DUPLICATE_LOGIN_ID("U003", "이미 사용 중인 아이디입니다.", HttpStatus.BAD_REQUEST),
  INVALID_USER_ROLE("U004", "유효하지 않은 사용자 권한입니다.", HttpStatus.BAD_REQUEST),

  // 인증 실패
  PHONE_NOT_VERIFIED("U005", "휴대폰 인증이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST),

  // 토큰 관련
  INVALID_TOKEN("U006", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
  TOKEN_EXPIRED("U007", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

  // 기타
  USER_NOT_FOUND("U008", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_INPUT("U009", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

  EMAIL_ALREADY_EXISTS("U010", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

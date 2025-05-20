package com.zzimple.global.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SmsErrorCode implements BaseErrorCode {

  // 1. 실제 전송 요청은 보냈으나, 결과가 실패로 돌아왔을 때
  SMS_SEND_FAILED("S001", "SMS 전송에 실패했습니다.",HttpStatus.INTERNAL_SERVER_ERROR),

  // 2. 요청 파라미터 (번호, 메시지 등)가 유효하지 않을 때
  INVALID_PARAMETER("S002", "잘못된 SMS 요청 파라미터입니다.",HttpStatus.BAD_REQUEST),

  // 3. 인증 토큰이 없거나 만료/무효화 되었을 때
  UNAUTHORIZED("S003", "인증 토큰이 유효하지 않습니다.",HttpStatus.UNAUTHORIZED),

  // 4. 권한은 있지만, 해당 가맹점에 SMS 전송 권한이 없을 때
  FORBIDDEN("S004", "SMS 전송 권한이 없습니다.",HttpStatus.FORBIDDEN),

  // 5. 메시지 길이가 API 제한(예: 2000자)을 초과했을 때
  MESSAGE_TOO_LONG("S005", "SMS 메시지 길이가 너무 깁니다.",HttpStatus.BAD_REQUEST),

  // 6. 외부 SMS 게이트웨이 연동 자체가 실패했을 때 (타임아웃, 커넥션 오류 등)
  EXTERNAL_SERVICE_ERROR("S006", "SMS 외부 서비스 연동 오류입니다.",HttpStatus.BAD_GATEWAY),

  // 7. 그 외 알 수 없는 예외가 발생했을 때
  UNKNOWN_ERROR("S999", "알 수 없는 SMS 오류입니다.",HttpStatus.INTERNAL_SERVER_ERROR),

  // 8. 인증번호가 Redis에 존재하지 않음 (만료)
  CERTIFICATION_CODE_EXPIRED("S007", "인증번호가 만료되었습니다.", HttpStatus.BAD_REQUEST),

  // 9. 인증번호가 일치하지 않음
  INVALID_CERTIFICATION_CODE("S008", "인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

  // 10. 인증번호 Redis 저장 실패
  REDIS_SAVE_FAIL("S010", "인증번호 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
package com.zzimple.estimate.guest.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MoveItemErrorCode implements BaseErrorCode {

  JSON_PARSE_FAIL("M5001", "짐 항목 JSON 파싱 실패", HttpStatus.INTERNAL_SERVER_ERROR),
  REDIS_SAVE_FAIL("M5002", "Redis 저장 실패", HttpStatus.INTERNAL_SERVER_ERROR),
  REDIS_REMOVE_FAIL("M5003", "Redis 항목 삭제 실패", HttpStatus.INTERNAL_SERVER_ERROR),
  JSON_SERIALIZE_FAIL("M5004", "짐 항목 JSON 직렬화 실패", HttpStatus.INTERNAL_SERVER_ERROR),

  INVALID_ITEM_TYPE_ID("M4041", "유효하지 않은 itemTypeId입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

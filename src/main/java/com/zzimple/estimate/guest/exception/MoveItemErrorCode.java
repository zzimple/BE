package com.zzimple.estimate.guest.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MoveItemErrorCode implements BaseErrorCode {

  JSON_PARSE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M001", "짐 항목 JSON 파싱 실패"),
  REDIS_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M002", "Redis 저장 실패"),
  REDIS_REMOVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M003", "Redis 항목 삭제 실패"),
  JSON_SERIALIZE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M004", "짐 항목 JSON 직렬화 실패");;

  private final HttpStatus status;
  private final String code;
  private final String message;
}

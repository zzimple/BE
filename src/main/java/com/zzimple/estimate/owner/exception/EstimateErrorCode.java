package com.zzimple.estimate.owner.exception;

import com.zzimple.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EstimateErrorCode implements BaseErrorCode {

  INVALID_DATE_FORMAT("EST001", "이사 날짜 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
  INVALID_MOVE_TYPE("EST002", "이사 유형이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  FAILED_TO_FETCH_ESTIMATES("EST003", "공개 견적서 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ESTIMATE_NOT_FOUND("EST004", "견적서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_MOVE_OPTION("EST005", "이사 옵션이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),  // ← 추가
  STORE_PRICE_SETTING_NOT_FOUND("EST006", "사장님의 단가 설정이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  ESTIMATE_ACCESS_DENIED("EST007", "해당 견적서에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),

  ESTIMATE_ALREADY_FINALIZED("EST008", "이미 확정된 견적서입니다.", HttpStatus.CONFLICT),
  INVALID_ESTIMATE_ITEMS("EST009", "견적 항목 정보가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
  INVALID_EXTRA_CHARGE("EST010", "추가금 항목이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  ESTIMATE_STORE_MISMATCH("EST011", "사장님 정보가 견적서와 일치하지 않습니다.", HttpStatus.FORBIDDEN),

  ALREADY_RESPONDED("EST012", "이미 해당 견적서에 응답하였습니다.", HttpStatus.CONFLICT),
  ESTIMATE_OWNER_RESPONSE_NOT_FOUND("EST013", "해당 사장님의 응답을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  NO_RESPONSES_FOUND("EST014", "응답이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  STORE_RESPONSE_NOT_FOUND("EST015", "사장님의 응답을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;

}

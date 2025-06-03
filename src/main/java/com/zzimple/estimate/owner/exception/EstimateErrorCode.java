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
  ESTIMATE_ACCESS_DENIED("EST007", "해당 견적서에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);


  private final String code;
  private final String message;
  private final HttpStatus status;

}

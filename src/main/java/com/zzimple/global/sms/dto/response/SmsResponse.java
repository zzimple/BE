package com.zzimple.global.sms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsResponse {
  @Schema(description = "응답 코드")
  private String resCode;

  @Schema(description = "응답 메시지")
  private String resMessage;

  @Schema(description = "응답 데이터")
  private ResData resData;

  @Getter
  @Builder
  public static class ResData {
    @Schema(description = "발신 번호")
    private String msgFrom;

    @Schema(description = "수신 번호")
    private String msgTo;

    @Schema(description = "메시지 상태", example = "O:성공, E:시스템오류, D:사업자오류, B:수신거부, X:요청실패(사업자 모두 실패)")
    private String messageStatus;

    @Schema(description = "메시지 타입", example = "1:SMS, 2:LMS")
    private String messageType;

    @Schema(description = "주문일자")
    private String ordDay;

    @Schema(description = "주문시간")
    private String ordTime;

    @Schema(description = "주문번호")
    private String ordNo;

    @Schema(description = "거래일자")
    private String trDay;

    @Schema(description = "거래시간")
    private String trTime;

    @Schema(description = "거래번호")
    private String trNo;
  }
}
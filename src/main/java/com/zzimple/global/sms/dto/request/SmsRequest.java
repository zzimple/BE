package com.zzimple.global.sms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메세지 보내기 Request")
public class SmsRequest {
  @NotBlank
  @Schema(description = "발신번호", example = "010-0000-0000")
  private String msgFrom;

  @NotBlank
  @Schema(description = "수신번호", example = "010-0000-0000")
  private String msgTo;

  @NotBlank
  @Schema(description = "메세지 타입", example = "\"1\"=SMS, \"2\"=LMS")
  private String messageType;

  @NotBlank
  @Size(max=2000)
  @Schema(description = "본문", example = "(개인정보 마스킹 필수)")
  private String message;

  @NotBlank
  @Pattern(regexp="\\d{8}")
  @Schema(description = "주문일자", example = "YYYYMMDD")
  private String ordDay;

  @NotBlank
  @Pattern(regexp="\\d{6}")
  @Schema(description = "주문시간", example = "HHmmss")
  private String ordTime;

  @NotBlank
  @Schema(description = "가맹점 유니크 주문번호")
  private String ordNo;

  @Schema(description = "선택")
  private String param1;

  @Schema(description = "선택")
  private String param2;
}
package com.zzimple.global.sms.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.sms.dto.request.SignupCodeRequest;
import com.zzimple.global.sms.dto.request.VerifySignupCodeRequest;
import com.zzimple.global.sms.dto.response.SmsResponse;
import com.zzimple.global.sms.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sms")
public class SmsController {

  private final SmsService smsService;

  @Operation(
      summary = "[ 모두 | 토큰 X | 휴대폰 인증번호 전송 ]",
      description =
          """
           **Parameters**  \n
           msgTo: 인증번호 받을 전화번호  \n

           **Returns**  \n
              resCode: 응답 코드 (예: 0이면 성공)  \n
              resMessage: 응답 메시지 (예: '성공')  \n
              resData: 인증번호 전송 결과 데이터  \n
              ┗ msgFrom: 발신자 번호  \n
              ┗ msgTo: 수신자 번호  \n
              ┗ messageStatus: 메시지 전송 상태 (예: 'O' - 완료)  \n
              ┗ messageType: 메시지 타입 (예: 'S' - SMS)  \n
              ┗ ordDay: 요청 날짜 (yyyyMMdd)  \n
              ┗ ordTime: 요청 시간 (HHmmss)  \n
              ┗ ordNo: 요청 고유 번호 (UUID)  \n
              ┗ trDay: 처리 날짜 (yyyyMMdd)  \n
              ┗ trTime: 처리 시간 (HHmmss)  \n
              ┗ trNo: 메시지 전송 고유 번호
           """)
  @PostMapping("/signup-code")
  public BaseResponse<SmsResponse> sendSignupCode(@RequestBody @Valid SignupCodeRequest request) {
    SmsResponse response = smsService.sendSignupCode(request.getMsgTo());
    return BaseResponse.success("인증번호가 전송되었습니다.", response);
  }

  @Operation(
      summary = "[ 모두 | 토큰 X | 휴대폰 인증번호 전송 인증 ]",
      description =
          """
              **Parameters**  \n
              phone: 인증할 전화번호  \n
              code: 인증번호  \n
              
              **Returns**  \n
              success: 성공여부  \n
              message: 메세지  \n
              """)
  @PostMapping("/signup-code/verify")
  public BaseResponse<Void> verifySignupCode(
      @RequestBody @Valid VerifySignupCodeRequest request) {
    smsService.verifySignupCode(request.getPhone(), request.getCode());
    return BaseResponse.success("인증번호가 확인되었습니다.", null);
  }
}

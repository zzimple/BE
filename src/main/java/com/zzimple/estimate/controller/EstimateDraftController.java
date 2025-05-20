package com.zzimple.estimate.controller;

import com.zzimple.estimate.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.dto.response.AddressDraftResponse;
import com.zzimple.estimate.dto.response.HolidayCheckResponse;
import com.zzimple.estimate.service.EstimateDraftService;
import com.zzimple.estimate.service.HolidayService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/estimates/draft")
@RequiredArgsConstructor
public class EstimateDraftController {

  private final EstimateDraftService estimateDraftService;
  private final HolidayService holidayService;

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 주소 임시 저장 ]",
      description =
          """
           **Parameters**  \n
           fromAddress: 출발 주소 정보 (JSON 객체) \s
           roadFullAddr: 전체 도로명 주소 (예: 서울특별시 강남구 테헤란로 223, 20층 (역삼동)) \s
           roadAddr: 기본 도로명 주소 \s
           addrDetail: 상세 주소 (예: 20층) \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s
           toAddress: 도착 주소 정보 (JSON 객체) \s
           roadFullAddr: 전체 도로명 주소 \s
           roadAddr: 기본 도로명 주소 \s
           addrDetail: 상세 주소 \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s

           **Returns**  \n
           roadAddr: 도로명 주소 (전체x)  \n
           message: 임시 저장 성공 여부  \n
           """)
  @PostMapping("/address")
  public ResponseEntity<BaseResponse<AddressDraftResponse>> saveAddress(@AuthenticationPrincipal CustomUserDetails user, @RequestBody AddressDraftSaveRequest request) {
    Long userId = user.getUserId();
    AddressDraftResponse response = estimateDraftService.saveAddressDraft(userId, request);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 공휴일 여부 확인 및 저장 ]",
      description =
          """
          **Parameters**  \n
          date: 확인할 날짜 (형식: yyyyMMdd)  \n
  
          **Returns**  \n
          isHoliday: 공휴일 여부 (Y / N)  \n
          dateName: 공휴일 이름 (예: 어린이날), 공휴일이 아닐 경우 null  \n
          """
  )
  @GetMapping("/holiday/check")
  public ResponseEntity<BaseResponse<HolidayCheckResponse>> checkHoliday(@RequestParam String date) {
    HolidayCheckResponse result = holidayService.checkHoliday(date);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}

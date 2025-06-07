package com.zzimple.estimate.owner.controller;

import com.zzimple.estimate.owner.dto.request.SaveItemBasePriceRequest;
import com.zzimple.estimate.owner.dto.request.SaveStorePriceSettingRequest;
import com.zzimple.estimate.owner.dto.response.SaveItemBasePriceResponse;
import com.zzimple.estimate.owner.dto.response.StorePriceSettingResponse;
import com.zzimple.estimate.owner.service.SaveItemBasePriceService;
import com.zzimple.estimate.owner.service.StorePriceSettingService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/owner/my")
@RequiredArgsConstructor
public class OwnerEstimateController {

  private final SaveItemBasePriceService saveItemBasePriceService;
  private final StorePriceSettingService storePriceSettingService;

  @Operation(
      summary = "[사장님 | 토큰 O | 물품 기본 단가 일괄 저장]",
      description = "사장님이 한 번에 여러 짐 항목의 기본 단가를 저장 또는 수정합니다."
  )
  @PostMapping("/estimate/default-prices")
  public ResponseEntity<BaseResponse<List<SaveItemBasePriceResponse>>> saveBasePrice(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody List<SaveItemBasePriceRequest> requests
  ) {
    Long storeId = userDetails.getStoreId();

    List<SaveItemBasePriceResponse> response = saveItemBasePriceService.saveOrUpdateAll(storeId,
        requests);
    return ResponseEntity.ok(BaseResponse.success("기본 단가 저장이 완료되었습니다.", response));
  }

  @Operation(
      summary = "[사장님 | 토큰 O | 마이페이지 추가요금 단가 저장]",
      description = "트럭/공휴일/손없는날/주말 단가 정보를 저장합니다."
  )
  @PostMapping("/price-setting")
  public ResponseEntity<BaseResponse<String>> savePriceSetting(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody SaveStorePriceSettingRequest request
  ) {

    Long storeId = userDetails.getStoreId();

    storePriceSettingService.savePriceSetting(storeId, request);
    return ResponseEntity.ok(BaseResponse.success("추가요금 단가 설정 저장 완료"));
  }

  @Operation(
      summary = "[사장님 | 토큰 O | 마이페이지 추가요금 단가 조회]",
      description = "트럭/공휴일/손없는날/주말 단가 정보를 조회합니다."
  )
  @GetMapping("/price-setting")
  public ResponseEntity<BaseResponse<StorePriceSettingResponse>> getPriceSetting(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long storeId = userDetails.getStoreId();
    StorePriceSettingResponse response = storePriceSettingService.getPriceSetting(storeId);
    return ResponseEntity.ok(BaseResponse.success(response));
  }
}

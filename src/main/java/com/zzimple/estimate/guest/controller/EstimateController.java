package com.zzimple.estimate.guest.controller;

import com.zzimple.estimate.guest.dto.response.EstimateListDetailResponse;
import com.zzimple.estimate.guest.service.GuestEstimateService;
import com.zzimple.global.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/view")
public class EstimateController {

  private final GuestEstimateService guestEstimateService;

  @GetMapping("/estimate/{estimateNo}")
  @Operation(
      summary = "[고객 | 토큰 O 사장님에게 온 견적서 상세 조회 + 사장 | 토큰 O 견적서 상세 조회]",
      description = "견적서의 상세 정보를 조회합니다."
  )
  @PreAuthorize("hasAnyRole('CUSTOMER','OWNER')")
  public ResponseEntity<BaseResponse<EstimateListDetailResponse>> getEstimateDetail(
      @PathVariable Long estimateNo
  ) {
    EstimateListDetailResponse response = guestEstimateService.getEstimateDetail(estimateNo);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

}

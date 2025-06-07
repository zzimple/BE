package com.zzimple.estimate.guest.controller;

import com.zzimple.estimate.guest.dto.response.EstimateListDetailResponse;
import com.zzimple.estimate.guest.dto.response.EstimateListResponse;
import com.zzimple.estimate.guest.dto.response.GuestEstimateRespondResult;
import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.guest.service.GuestEstimateService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/guest/my")
public class GuestEstimateController {

  private final GuestEstimateService guestEstimateService;

  @GetMapping("/estimate/list")
  @Operation(
      summary = "[ 고객 | 토큰 O | 고객이 받은 견적서 조회 ]",
      description = "로그인한 고객이 받은 확정 견적서 목록을 페이징으로 조회합니다."
  )
  public ResponseEntity<BaseResponse<PagedResponse<EstimateListResponse>>> getMyEstimates(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PagedResponse<EstimateListResponse> response = guestEstimateService.getAcceptedEstimatesForUser(userDetails.getUserId(), page, size);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  @GetMapping("/estimate/{estimateNo}")
  @Operation(
      summary = "[고객 | 토큰 O 사장님에게 온 견적서 상세 조회]",
      description = "사장님이 보낸 확정 견적서의 상세 정보를 고객이 확인합니다."
  )
  public ResponseEntity<BaseResponse<EstimateListDetailResponse>> getEstimateDetail(
      @PathVariable Long estimateNo
  ) {
    EstimateListDetailResponse response = guestEstimateService.getEstimateDetail(estimateNo);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  @PostMapping("/estimates/{estimateNo}/respond")
  @Operation(
      summary = "[고객 | 토큰 O 사장님에게 온 견적서 수락/거절]",
      description = "사장님이 보낸 확정 견적서를 수락 여부를 결정합니다."
  )
  public ResponseEntity<BaseResponse<GuestEstimateRespondResult>> respondEstimate(
      @PathVariable Long estimateNo,
      @RequestParam boolean accepted
  ) {
    GuestEstimateRespondResult result = GuestEstimateRespondResult.builder()
        .estimateNo(estimateNo)
        .accepted(accepted)
        .build();

    return ResponseEntity.ok(BaseResponse.success("견적서 응답 처리 완료",result));
  }
}

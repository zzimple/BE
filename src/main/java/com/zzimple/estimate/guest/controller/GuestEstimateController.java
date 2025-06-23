package com.zzimple.estimate.guest.controller;

import com.zzimple.estimate.guest.dto.EstimateResponseList;
import com.zzimple.estimate.guest.dto.response.PagedMyEstimates;
import com.zzimple.estimate.owner.dto.request.EstimateRespondRequest;
import com.zzimple.estimate.guest.dto.response.EstimateListResponse;
import com.zzimple.estimate.guest.dto.response.GuestEstimateRespondResult;
import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.guest.service.GuestEstimateService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public ResponseEntity<BaseResponse<PagedResponse<EstimateListResponse>>> getMyConfirmEstimates(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PagedResponse<EstimateListResponse> response = guestEstimateService.getAcceptedEstimatesForUser(userDetails.getUserId(), page, size);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  @GetMapping("all/list/estimates")
  public ResponseEntity<BaseResponse<PagedMyEstimates>> getMyEstimates(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {

    PagedMyEstimates estimates = guestEstimateService.getMyEstimates(userDetails.getUserId(), page, size);
    return ResponseEntity.ok(BaseResponse.success(estimates));
  }

  @GetMapping("/{estimateNo}/responses")
  public ResponseEntity<BaseResponse<EstimateResponseList>> getResponses(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long estimateNo
  ) {
    EstimateResponseList responseList = guestEstimateService.getResponses(estimateNo, userDetails.getUserId());
    return ResponseEntity.ok(BaseResponse.success(responseList));
  }

  @PutMapping("/{estimateNo}/respond/{storeId}")
  public ResponseEntity<BaseResponse<GuestEstimateRespondResult>> respondToEstimate(
      @PathVariable Long estimateNo,
      @PathVariable Long storeId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId(); // 고객 ID

    GuestEstimateRespondResult result = guestEstimateService.respondToEstimate(
        estimateNo,
        storeId,     // ✅ 고객이 선택한 가게
        userId                    // 고객 본인
    );

    return ResponseEntity.ok(BaseResponse.success(result));
  }

}

package com.zzimple.staff.controller;

import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.staff.dto.request.StaffTimeOffRequest;
import com.zzimple.staff.dto.response.StaffTimeOffResponse;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.repository.StaffTimeOffepository;
import com.zzimple.staff.service.StaffTimeOffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff/time-off")
@RequiredArgsConstructor
@Slf4j
public class StaffTimeOffController {

  private final StaffTimeOffService staffTimeOffService;

  // 휴무 요청하는 api
  @Operation(
      summary = "[ 직원 | 토큰 O ] 직원 휴무 등록",
      description = "직원이 휴무 등록을 합니다."
  )
  @PostMapping("/request")
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<BaseResponse<StaffTimeOffResponse>> requestTimeOff(
      @AuthenticationPrincipal CustomUserDetails staff,
      @RequestBody @Valid StaffTimeOffRequest request
  ) {

    Long userId = staff.getUserId();

    StaffTimeOffResponse response = staffTimeOffService.apply(userId,request);
    return ResponseEntity.ok(
        BaseResponse.success("휴무 신청이 완료되었습니다.", response)
    );
  }

  // 직원 휴무 요청 목록 조회
  @GetMapping("/pending")
  @PreAuthorize("hasRole('OWNER')")
  @Operation(
      summary = "[ 사장 | 토큰 O ] 직원 휴무 리스트",
      description = "직원 휴무 요청 온 리스트"
  )
  public ResponseEntity<BaseResponse<PagedResponse<StaffTimeOffResponse>>> getPendingRequests(
      @RequestParam int page,
      @RequestParam int size,
      @AuthenticationPrincipal CustomUserDetails user
  ) {

    Long userId = user.getUserId();

    PagedResponse<StaffTimeOffResponse> responseList = staffTimeOffService.listPending(userId, page, size);

    return ResponseEntity.ok(BaseResponse.success("휴무 요청 목록 조회 성공", responseList));
  }

  // 휴무 요청 승인/거절
  @PatchMapping("/decide/{staffTimeOffId}")
  @PreAuthorize("hasRole('OWNER')")
  @Operation(summary = "[사장 | 토큰 O] 휴무 요청 승인 또는 반려")
  public ResponseEntity<BaseResponse<StaffTimeOffResponse>> decideRequest(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long staffTimeOffId,
      @RequestParam("status") Status status
  ) {
    Long storeId = userDetails.getStoreId();
    StaffTimeOffResponse response = staffTimeOffService.decide(staffTimeOffId, status, storeId);
    return ResponseEntity.ok(BaseResponse.success("요청 처리 완료", response));
  }
}

package com.zzimple.staff.controller;

import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.staff.dto.request.StaffTimeOffRequest;
import com.zzimple.staff.dto.response.StaffAssignmentResponse;
import com.zzimple.staff.dto.response.StaffScheduleCalendarItem;
import com.zzimple.staff.dto.response.StaffTimeOffResponse;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.service.StaffScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class StaffScheduleController {

  private final StaffScheduleService staffScheduleService;

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

    StaffTimeOffResponse response = staffScheduleService.apply(userId,request);
    return ResponseEntity.ok(
        BaseResponse.success("휴무 신청이 완료되었습니다.", response)
    );
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
    StaffTimeOffResponse response = staffScheduleService.decide(staffTimeOffId, status, storeId);
    return ResponseEntity.ok(BaseResponse.success("요청 처리 완료", response));
  }

  // 본인 휴무 내역 조회
  @Operation(
      summary = "[ 직원 | 토큰 O ] 본인 휴무 내역 조회",
      description = "로그인한 직원이 자신이 신청한 휴무 내역을 페이징 조회합니다."
  )
  @GetMapping("/me")
  @PreAuthorize("hasRole('STAFF')")
  public PagedResponse<StaffTimeOffResponse> getMyTimeOff(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Long userId = userDetails.getUserId();

    return staffScheduleService.listMyRequests(userId, page, size);
  }


  @Operation(summary = "[사장 | 토큰 O] 대기중 휴무 요청 조회")
  @GetMapping("/list/pending")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<BaseResponse<List<StaffTimeOffResponse>>> pending(
      @AuthenticationPrincipal CustomUserDetails user
  ) {

    Long userId = user.getUserId();

    return ResponseEntity.ok(
        BaseResponse.success("대기중 휴무 요청 조회", staffScheduleService.listPendingRequests(userId))
    );
  }

  @Operation(summary = "[사장 | 토큰 O] 승인된 휴무 요청 조회")
  @GetMapping("/list/approved")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<BaseResponse<List<StaffTimeOffResponse>>> approved(
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    Long userId = user.getUserId();

    return ResponseEntity.ok(
        BaseResponse.success("승인된 휴무 요청 조회", staffScheduleService.listApprovedRequests(userId))
    );
  }

  @Operation(summary = "[사장 | 토큰 O] 거절된 휴무 요청 조회")
  @GetMapping("/list/rejected")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<BaseResponse<List<StaffTimeOffResponse>>> rejected(
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    Long userId = user.getUserId();

    return ResponseEntity.ok(
        BaseResponse.success("거절된 휴무 요청 조회", staffScheduleService.listRejectedRequests(userId))
    );
  }

  @GetMapping("/calendar")
  public ResponseEntity<List<StaffScheduleCalendarItem>> getMyCalendar(
      @RequestParam String yearMonth,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<StaffScheduleCalendarItem> result = staffScheduleService.getMyMonthlyCalendar(
        userDetails.getUserId(), yearMonth);
    return ResponseEntity.ok(result);
  }
}

package com.zzimple.owner.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.owner.dto.request.AssignStaffDateRequest;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.owner.service.StaffAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/schedule/{estimateNo}")
public class StaffScheduleController {

  private final StaffAssignmentService staffAssignmentService;

  @PostMapping("/assign")
  public ResponseEntity<BaseResponse<AssignStaffDateResponse>> assignStaff(
      @PathVariable("estimateNo") Long estimateNo,
      @RequestBody @Valid AssignStaffDateRequest request
  ) {

    Long staffId = request.getStaffId();

    AssignStaffDateResponse response = staffAssignmentService.assignWithDate(estimateNo, staffId, request.getWorkDate());

    return ResponseEntity.ok(BaseResponse.success("직원 스케줄 배정에 성공했습니다", response));
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<BaseResponse<AssignStaffDateResponse>> updateAssignmentDate(
      @PathVariable Long estimateNo,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid AssignStaffDateRequest request
  ) {

    Long userId = userDetails.getUserId();

    AssignStaffDateResponse response = staffAssignmentService.updateAssignmentDate(estimateNo, userId, request.getWorkDate());

    return ResponseEntity.ok(BaseResponse.success("직원 스케줄 변경에 성공했습니다", response));
  }
}

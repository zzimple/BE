package com.zzimple.owner.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.owner.dto.request.AssignStaffDateRequest;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.owner.dto.response.AvailableStaffResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.service.StaffAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@RequestMapping("/owner/schedule")
public class StaffScheduleController {

  private final StaffAssignmentService staffAssignmentService;
  private final OwnerRepository ownerRepository;

  @PostMapping("/{estimateNo}/assign")
  public ResponseEntity<BaseResponse<AssignStaffDateResponse>> assign(
      @PathVariable Long estimateNo,
      @RequestParam Long staffId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    // 1) 로그인된 사장의 User ID로 Owner Entity 조회
    Owner owner = ownerRepository.findByUserId(userDetails.getUserId())
        .orElseThrow(() -> new CustomException(OwnerErrorCode.OWNER_NOT_FOUND));
    Long ownerId = owner.getId();

    // 3) 서비스 호출
    AssignStaffDateResponse resp =
        staffAssignmentService.assignWithDate(estimateNo, staffId);
    return ResponseEntity.ok(BaseResponse.success("직원 스케줄 배정에 성공했습니다", resp));
  }

  @GetMapping("/{estimateNo}/available-staff")
  public ResponseEntity<BaseResponse<List<AvailableStaffResponse>>> getAvailableStaff(
      @PathVariable Long estimateNo,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();

    List<AvailableStaffResponse> staffList = staffAssignmentService.getAvailableStaffList(estimateNo, userId);

    return ResponseEntity.ok(BaseResponse.success("사용 가능한 직원 목록입니다.", staffList));
  }

}

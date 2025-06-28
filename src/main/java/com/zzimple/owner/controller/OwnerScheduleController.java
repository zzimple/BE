package com.zzimple.owner.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.owner.dto.response.AssignedStaffListResponse;
import com.zzimple.owner.dto.response.AvailableStaffResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.service.StaffAssignmentService;
import com.zzimple.user.entity.User;
import com.zzimple.user.exception.UserErrorCode;
import com.zzimple.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/schedule")
public class OwnerScheduleController {

  private final StaffAssignmentService staffAssignmentService;
  private final OwnerRepository ownerRepository;
  private final UserRepository userRepository;

  @PostMapping("/{estimateNo}/assign")
  public ResponseEntity<BaseResponse<List<AssignStaffDateResponse>>> assign(
      @PathVariable Long estimateNo,
      @RequestParam List<Long> staffIds
  ) {
    // 2) 서비스 호출 (한 명 혹은 여러 명)
    List<AssignStaffDateResponse> respList =
        staffAssignmentService.assignWithDate(estimateNo, staffIds);

    return ResponseEntity.ok(
        BaseResponse.success("직원 스케줄 배정에 성공했습니다", respList)
    );
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

  // 새로 추가
  @GetMapping("/{estimateNo}/assigned-staff")
  public ResponseEntity<BaseResponse<AssignedStaffListResponse>> getAssignedStaff(
      @PathVariable Long estimateNo,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    User user = userRepository.findById(userDetails.getUserId())
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Long userId = user.getId();

    // 2) 서비스 호출
    AssignedStaffListResponse response =
        staffAssignmentService.getAssignedStaffList(estimateNo, userId);

    // 3) 결과 반환
    return ResponseEntity.ok(BaseResponse.success("배정된 직원 목록입니다.", response));
  }
}

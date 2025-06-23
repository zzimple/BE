package com.zzimple.staff.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.staff.dto.request.OwnerApproveRequest;
import com.zzimple.staff.dto.request.StaffsendApprovalRequest;
import com.zzimple.staff.dto.response.OwnerApproveResponse;
import com.zzimple.staff.dto.response.StaffListResponse;
import com.zzimple.staff.dto.response.StaffProfileResponse;
import com.zzimple.staff.dto.response.StaffsendApprovalResponse;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

  private final StaffService staffService;


  @Operation(
      summary = "[ 직원 | 토큰 O | 직원 승인 요청 ]",
      description = "직원이 사장님께 승인 요청 보냄"
  )

  @PostMapping("/request")
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<BaseResponse<Void>> requestApproval(@AuthenticationPrincipal CustomUserDetails user, @RequestBody StaffsendApprovalRequest request) {

    Long userId = user.getUserId();
    log.info("[직원 요청] 승인 요청 시작 - staffId: {}, ownerPhone: {}", userId, request.getOwnerPhoneNumber());

    StaffsendApprovalResponse response = staffService.requestApproval(userId, request);
    log.info("[직원 요청] 승인 요청 완료 - staffId: {}, result: {}", userId, response.isSuccess());

    return ResponseEntity.ok(BaseResponse.success("사장님에게 승인 요청을 보냈습니다.", null));
  }

  @Operation(
      summary = "[ 사장 | 토큰 O | 직원 승인 수락/거절 ]",
      description =
          """
           **Parameters**  \n
           staffId: staff 고유 아이디  \n
           status: 승인/거절  \n

           **Returns**  \n
           status: 처리된 승인 상태  \n
           message: 결과 메시지  \n
           """)
  @PatchMapping("/approve")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<BaseResponse<OwnerApproveResponse>> approveStaff(@RequestBody OwnerApproveRequest request, @AuthenticationPrincipal CustomUserDetails user) {

    Long userId = user.getUserId();

    log.info("[사장님 승인] 사장님 승인 요청 - staffId: {}, userId: {}", request.getStaffId(), userId);

    // 서비스에서 상태 반환받기
    Status resultStatus = staffService.approveStaff(request.getStaffId(), request.getStatus(), userId);

    // 메시지 결정
    String message = switch (resultStatus) {
      case APPROVED -> "승인이 완료되었습니다.";
      case REJECTED -> "거절이 완료되었습니다.";
      default -> "알 수 없는 상태입니다.";
    };

    OwnerApproveResponse response = new OwnerApproveResponse(resultStatus);
    return ResponseEntity.ok(BaseResponse.success(message, response));
  }

  @Operation(
      summary = "[ 사장 | 토큰 O ] 직원 전체 목록 조회",
      description = "사장님 소속의 직원 전체 리스트를 status 기준으로 내려줍니다."
  )
  @GetMapping("/list")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<BaseResponse<List<StaffListResponse>>> getStaffList(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {

    Long storeId = userDetails.getStoreId();

    List<StaffListResponse> staffList = staffService.getStaffListByOwner(storeId);
    return ResponseEntity.ok(BaseResponse.success("조회 성공", staffList));
  }

  @Operation(
      summary = "[ 직원 | 토큰 O ] 직원 프로필 조회",
      description = "직원 본인의 프로필을 조회합니다."
  )
  @GetMapping("/profile")
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<BaseResponse<StaffProfileResponse>> getStaffProfile(
      @AuthenticationPrincipal CustomUserDetails user) {

    Long userId = user.getUserId();

    StaffProfileResponse profile = staffService.getStaffProfile(userId);

    return ResponseEntity.ok(BaseResponse.success("직원 프로필 조회 성공", profile));
  }
}
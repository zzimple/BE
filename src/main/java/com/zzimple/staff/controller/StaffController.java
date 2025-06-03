package com.zzimple.staff.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.staff.dto.request.OwnerApproveRequest;
import com.zzimple.staff.dto.request.StaffsendApprovalRequest;
import com.zzimple.staff.dto.response.OwnerApproveResponse;
import com.zzimple.staff.dto.response.StaffsendApprovalResponse;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
      description =
          """
           **Parameters**  \n
           ownerPhoneNumber: 사장님 전화번호  \n

           **Returns**  \n
           Success: 요청 성공 여부  \n
           message: 결과 메시지  \n
           """)
  @PostMapping("/request")
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<BaseResponse<Void>> requestApproval(@AuthenticationPrincipal CustomUserDetails user, @RequestBody StaffsendApprovalRequest request) {

    Long staffId = user.getUserId();
    log.info("[직원 요청] 승인 요청 시작 - staffId: {}, ownerPhone: {}", staffId, request.getOwnerPhoneNumber());

    StaffsendApprovalResponse response = staffService.requestApproval(staffId, request);
    log.info("[직원 요청] 승인 요청 완료 - staffId: {}, result: {}", staffId, response.isSuccess());

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

    Long ownerId = user.getUserId();

    log.info("[사장님 승인] 사장님 승인 요청 - staffId: {}, ownerId: {}", request.getStaffId(), ownerId);

    // 서비스에서 상태 반환받기
    Status resultStatus = staffService.approveStaff(request.getStaffId(), request.getStatus(), ownerId);

    // 메시지 결정
    String message = switch (resultStatus) {
      case APPROVED -> "승인이 완료되었습니다.";
      case REJECTED -> "거절이 완료되었습니다.";
      default -> "알 수 없는 상태입니다.";
    };

    OwnerApproveResponse response = new OwnerApproveResponse(resultStatus);
    return ResponseEntity.ok(BaseResponse.success(message, response));
  }
}
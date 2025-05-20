package com.zzimple.staff.service;

import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.global.exception.StaffErrorCode;
import com.zzimple.staff.dto.request.StaffsendApprovalRequest;
import com.zzimple.staff.dto.response.StaffsendApprovalResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

  private final StaffRepository staffRepository;
  private final UserRepository userRepository;

  // 사장님께 승인 요청
  @Transactional
  public StaffsendApprovalResponse requestApproval(Long staffId, StaffsendApprovalRequest request) {
    // 실제 유저를 조회하여 STAFF 권한 확인
    User user = userRepository.findById(staffId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

    if (user.getRole() != UserRole.STAFF) {
      log.warn("[승인 요청 실패] STAFF 권한 아님 - userId: {}", staffId);
      throw new CustomException(StaffErrorCode.INVALID_STAFF_ROLE);
    }

    // 사장님 확인
    User owner = userRepository.findByPhoneNumber(request.getOwnerPhoneNumber())
        .orElseThrow(() -> {
          log.warn("[승인 요청 실패] 존재하지 않는 사장님 전화번호: {}", request.getOwnerPhoneNumber());
          return new CustomException(StaffErrorCode.OWNER_NOT_FOUND);
        });

    // 사장님이 owner 권한인지 아닌지
    if (owner.getRole() != UserRole.OWNER) {
      log.warn("[승인 요청 실패] OWNER 권한 아님 - userId: {}", owner.getId());
      throw new CustomException(StaffErrorCode.INVALID_OWNER_ROLE);
    }

    // 이미 요청된 관계
    if (staffRepository.existsByStaffIdAndOwnerId(staffId, owner.getId())) {
      log.warn("[승인 요청 실패] 이미 요청된 관계 - staffId: {}, ownerId: {}", staffId, owner.getId());
      throw new CustomException(StaffErrorCode.APPROVAL_ALREADY_REQUESTED);
    }

    // staff 엔티티
    Staff staff = Staff.builder()
        .staffId(staffId)
        .ownerId(owner.getId())
        .status(Status.PENDING)
        .build();

    staffRepository.save(staff);

    log.info("[승인 요청 성공] 요청 완료 - staffId: {}, ownerId: {}", staffId, owner.getId());

    return new StaffsendApprovalResponse(true);
  }

  // 사장님 승인 메소드
  @Transactional
  public Status approveStaff(Long staffId, Status status, Long ownerId) {
    Staff staff = staffRepository.findByStaffId(staffId)
        .orElseThrow(() -> {
          log.warn("[승인 실패] 존재하지 않는 직원 - staffId: {}", staffId);
          return new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND);
        });

    if (!staff.getOwnerId().equals(ownerId)) {
      log.warn("[승인 실패] 소속 사장님 불일치 - 요청자: {}, 직원 소속: {}", ownerId, staff.getOwnerId());
      throw new CustomException(StaffErrorCode.STAFF_OWNER_MISMATCH);
    }

    if (status == Status.REJECTED) {
      log.info("[승인 처리] 요청 거절 - staffId: {}", staffId);
      staffRepository.delete(staff);
      return Status.REJECTED;
    }

    staff.setStatus(Status.APPROVED);
    staffRepository.save(staff);

    log.info("[승인 처리] 요청 승인 완료 - staffId: {}", staffId);
    return Status.APPROVED;
  }
}

package com.zzimple.staff.service;

import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.exception.StoreErrorCode;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.dto.response.StaffListResponse;
import com.zzimple.staff.exception.StaffErrorCode;
import com.zzimple.staff.dto.request.StaffsendApprovalRequest;
import com.zzimple.staff.dto.response.StaffsendApprovalResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.repository.UserRepository;
import java.util.List;
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
  private final StoreRepository storeRepository;
  private final OwnerRepository ownerRepository;

  // 사장님께 승인 요청
  @Transactional
  public StaffsendApprovalResponse requestApproval(Long userId, StaffsendApprovalRequest request) {

    // 실제 유저를 조회하여 STAFF 권한 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

    if (user.getRole() != UserRole.STAFF) {
      log.warn("[승인 요청 실패] STAFF 권한 아님 - userId: {}", userId);
      throw new CustomException(StaffErrorCode.INVALID_STAFF_ROLE);
    }

    // 사장님 확인
    User ownerUser = userRepository.findByPhoneNumber(request.getOwnerPhoneNumber())
        .orElseThrow(() -> {
          log.warn("[승인 요청 실패] 존재하지 않는 사장님 전화번호: {}", request.getOwnerPhoneNumber());
          return new CustomException(StaffErrorCode.OWNER_NOT_FOUND);
        });

    if (ownerUser.getRole() != UserRole.OWNER) {
      log.warn("[승인 요청 실패] 유저 ID {} - OWNER 권한 아님 (현재: {})", ownerUser.getId(), ownerUser.getRole());
      throw new CustomException(StaffErrorCode.INVALID_OWNER_ROLE);
    }

    Long ownerUserId = ownerUser.getId();

    Owner owner = ownerRepository.findByUserId(ownerUserId)
        .orElseThrow(() -> {
          log.warn("[승인 요청 실패] 유저 ID {} - Owner 정보 없음", ownerUser.getId());
          return new CustomException(StaffErrorCode.OWNER_NOT_FOUND);
        });

    Store store = storeRepository.findByOwnerUserId(owner.getId())
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    Long storeId = store.getId();

    // 이미 요청된 관계
    if (staffRepository.existsByUserIdAndOwnerId(userId, ownerUser.getId())) {
      log.warn("[승인 요청 실패] 이미 요청된 관계 - staffId: {}, ownerId: {}", userId, ownerUser.getId());
      throw new CustomException(StaffErrorCode.APPROVAL_ALREADY_REQUESTED);
    }

    // staff 엔티티
    Staff staff = Staff.builder()
        .userId(userId)
        .ownerId(owner.getId())
        .storeId(storeId)
        .status(Status.PENDING)
        .build();

    staffRepository.save(staff);

    log.info("[승인 요청 성공] Staff ID: {}, Owner ID: {}, Store ID: {}", userId, owner.getId(), storeId);

    return new StaffsendApprovalResponse(true);
  }

  // 사장님 승인 메소드
  @Transactional
  public Status approveStaff(Long staffId, Status status, Long storeId) {

    Staff staff = staffRepository.findById(staffId)
        .orElseThrow(() -> {
          log.warn("[승인 실패] 존재하지 않는 직원 - staffId: {}", staffId);
          return new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND);
        });

    if (!staff.getStoreId().equals(storeId)) {
      log.warn("[승인 실패] 소속 사장님 불일치 - 요청자 (가게 번호): {}, 직원이 요청한 사장님: {}", storeId, staff.getStoreId());
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

  public List<StaffListResponse> getStaffListByOwner(Long storeId) {
    List<Staff> staffList = staffRepository.findByStoreId(storeId);

    return staffList.stream()
        .map(staff -> {
          User user = userRepository.findById(staff.getUserId())
              .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));
          return StaffListResponse.from(staff, user);
        })
        .toList();
  }

}

package com.zzimple.staff.service;

import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.dto.request.StaffTimeOffRequest;
import com.zzimple.staff.dto.response.StaffTimeOffResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.entity.StaffTimeOff;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.exception.StaffErrorCode;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.staff.repository.StaffTimeOffepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffTimeOffService {
  private final StaffTimeOffepository staffTimeOffepository;
  private final StaffRepository staffRepository;
  private final StoreRepository storeRepository;
  private final UserRepository userRepository;

  // 1) 직원 휴무 신청
  public StaffTimeOffResponse apply(Long userId, StaffTimeOffRequest request) {

    log.info("[휴무 신청] 시작 - userId: {}, startDate: {}, endDate: {}, reason: {}",
        userId, request.getStartDate(), request.getEndDate(), request.getReason());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 유저입니다."));

    // 1) 직원 존재 확인
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 직원입니다."));

    String staffName = user.getUserName();

    Long storeId = staff.getStoreId();

    // 4) 엔티티 생성
    StaffTimeOff staffTimeOff = StaffTimeOff.builder()
        .staffName(staffName)
        .ownerId(staff.getOwnerId())
        .staffId(staff.getStaffId())
        .storeId(storeId)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .type(request.getType())
        .reason(request.getReason())
        .status(Status.PENDING)
        .build();

    // 5) 저장
    StaffTimeOff saved = staffTimeOffepository.save(staffTimeOff);

    log.info("[휴무 신청] 완료 - requestId: {}, staffId: {}, storeId: {}",
        saved.getStaffTimeOffId(), staffName, storeId);

    return new StaffTimeOffResponse(
        staffTimeOff.getStaffTimeOffId(),
        staffTimeOff.getStaffName(),
        staffTimeOff.getStatus(),
        staffTimeOff.getStartDate(),
        staffTimeOff.getEndDate(),
        staffTimeOff.getType(),
        staffTimeOff.getReason()
    );
  }

  // 엔티티 → DTO 변환
  private StaffTimeOffResponse toResponse(StaffTimeOff e) {
    return StaffTimeOffResponse.builder()
        .staffTimeOffId(e.getStaffTimeOffId())
        .staffName(e.getStaffName())
        .startDate(e.getStartDate())
        .endDate(e.getEndDate())
        .type(e.getType())
        .reason(e.getReason())
        .status(e.getStatus())
        .build();
  }

  // 사장님 승인/거절
  @Transactional
  public StaffTimeOffResponse decide(Long staffId, Status status, Long storeId) {

    StaffTimeOff reqeust_staffId = staffTimeOffepository.findById(staffId)
        .orElseThrow(() -> new CustomException(StaffErrorCode.REQUEST_NOT_FOUND));

    // 소유권 검증
    if (!reqeust_staffId.getStoreId().equals(storeId)) {
      throw new CustomException(StaffErrorCode.INVALID_STORE_ASSIGNMENT);
    }

    if (status == Status.APPROVED) {
      reqeust_staffId.setStatus(Status.APPROVED);
      log.info("[휴무 승인] requestId={}, staffId={} added to calendar", reqeust_staffId, reqeust_staffId.getStaffName());
    } else {
      reqeust_staffId.setStatus(Status.REJECTED);
      log.info("[휴무 반려] requestId={}, staffId={}", reqeust_staffId, reqeust_staffId.getStaffName());
    }
    StaffTimeOff updated = staffTimeOffepository.save(reqeust_staffId);
    return toResponse(updated);
  }

  // 사장 - 휴무 내역 리스트
  private List<StaffTimeOffResponse> listByStoreAndStatus(Long userId, Status status) {

    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> {
          log.warn("매장 정보 없음 - ownerUserId: {}", userId);
          return new EntityNotFoundException("해당 owner의 매장을 찾을 수 없습니다.");
        });

    Long storeId = store.getId();

    List<StaffTimeOff> timeOffs = staffTimeOffepository.findAllByStoreIdAndStatus(storeId, status);

    return timeOffs.stream()
        .map(StaffTimeOffResponse::from)
        .collect(Collectors.toList());
  }

  public PagedResponse<StaffTimeOffResponse> listMyRequests(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> {
          return new EntityNotFoundException("해당 직원을 찾을 수 없습니다.");
        });

    Long staffId = staff.getStaffId();

    Page<StaffTimeOff> pageResult = staffTimeOffepository.findByStaffId(staffId, pageable);

    List<StaffTimeOffResponse> content = pageResult.getContent().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());

    return PagedResponse.<StaffTimeOffResponse>builder()
        .content(content)
        .page(pageResult.getNumber())
        .size(pageResult.getSize())
        .totalElements(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .last(pageResult.isLast())
        .build();
  }


  @Transactional(readOnly = true)
  public List<StaffTimeOffResponse> listPendingRequests(Long userId) {
    return listByStoreAndStatus(userId, Status.PENDING);
  }

  @Transactional(readOnly = true)
  public List<StaffTimeOffResponse> listApprovedRequests(Long userId) {
    return listByStoreAndStatus(userId, Status.APPROVED);
  }

  @Transactional(readOnly = true)
  public List<StaffTimeOffResponse> listRejectedRequests(Long userId) {
    return listByStoreAndStatus(userId, Status.REJECTED);
  }

}

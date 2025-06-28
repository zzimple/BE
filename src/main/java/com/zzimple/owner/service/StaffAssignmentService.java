package com.zzimple.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.entity.EstimateOwnerResponse;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.EstimateOwnerResponseRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.owner.dto.response.AssignedStaffListResponse;
import com.zzimple.owner.dto.response.AvailableStaffResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.dto.response.StaffAssignmentResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.entity.StaffAssignment;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.repository.StaffAssignmentRepository;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.staff.repository.StaffTimeOffepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffAssignmentService {

  private final EstimateRepository estimateRepository;
  private final StaffRepository staffRepository;
  private final StaffAssignmentRepository staffAssignmentRepository;
  private final UserRepository userRepository;
  private final StaffTimeOffepository staffTimeOffepository;
  private final StoreRepository storeRepository;

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter ESTIMATE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");
  private final OwnerRepository ownerRepository;
  private final EstimateOwnerResponseRepository estimateOwnerResponseRepository;

//  @Transactional
//  public AssignStaffDateResponse assignWithDate(Long estimateNo, Long staffId) {
//    log.info("[직원 배정 시작] estimateNo={}, staffId={}", estimateNo, staffId);
//
//    // 1) Staff 조회
//    Staff staff = staffRepository.findByStaffId(staffId)
//        .orElseThrow(() -> new EntityNotFoundException("직원 없음 staffId=" + staffId));
//
//    // 2) Estimate 조회
//    Estimate estimate = estimateRepository.findById(estimateNo)
//        .orElseThrow(() -> new EntityNotFoundException("견적 없음 id=" + estimateNo));
//
//
//// 🔐 ✅ 사장님이 해당 견적서의 confirmedStoreId와 일치하는지 검증 필요
//    if (!EstimateStatus.CONFIRMED.equals(estimate.getStatus())) {
//      throw new IllegalStateException("아직 고객이 견적을 확정하지 않았습니다.");
//    }
//
//    // 3) User 조회
//    User user = userRepository.findById(staff.getUserId())
//        .orElseThrow(() -> new EntityNotFoundException("유저 없음 id=" + staff.getUserId()));
//    String staffName = user.getUserName();
//
//    // 4) 중복 배정 확인
//    staffAssignmentRepository.findByEstimateNoAndStaffId(estimateNo, staffId)
//        .ifPresent(a -> { throw new IllegalStateException("이미 배정된 직원입니다."); });
//
//    // 5) 날짜 포맷 변환
//    // 5) LocalDate로 변환 (✔️ workDate 타입이 LocalDate일 경우 바로 사용 가능)
//    LocalDate workDate = LocalDate.parse(estimate.getMoveDate(), ESTIMATE_DATE_FORMAT);
//    log.info("[파싱된 작업 날짜] {}", workDate);
//
//    // 6) 엔티티 생성 및 저장
//    StaffAssignment assignment = StaffAssignment.builder()
//        .estimateNo(estimateNo)
//        .staffId(staffId)
//        .staffName(staffName)
//        .workDate(workDate)
//        .build();
//    staffAssignmentRepository.save(assignment);
//
//    log.info("[직원 배정 완료] staffId={}, staffName={}, workDate={}",
//        staffId, staffName, workDate);
//
//    // 7) 응답 반환
//    return AssignStaffDateResponse.builder()
//        .staffId(staffId)
//        .staffName(staffName)
//        .workDate(workDate)
//        .build();
//  }

  @Transactional
  public List<AssignStaffDateResponse> assignWithDate(Long estimateNo, List<Long> staffIds) {
    // 0) 직원 ID 목록 검증
    if (staffIds == null || staffIds.isEmpty()) {
      throw new IllegalArgumentException("적어도 한 명의 직원 ID가 필요합니다.");
    }

    // 1) Estimate 상태 확인 (고객 확정 여부)
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적 없음 id=" + estimateNo));

    if (!EstimateStatus.CONFIRMED.equals(estimate.getStatus())) {
      throw new IllegalStateException("아직 고객이 견적을 확정하지 않았습니다.");
    }


    // 2) Owner 권한 검증 (견적 응답 storeId와 로그인된 사장 일치 여부)
    EstimateOwnerResponse confirmed = estimateOwnerResponseRepository
        .findByEstimateNoAndStatus(estimateNo, EstimateStatus.CONFIRMED)
        .orElseThrow(() -> new EntityNotFoundException(
            "사장 확정 응답 없음 estimateNo=" + estimateNo));

    // 로그인된 사장 정보 조회
    Owner owner = ownerRepository.findByUserId(confirmed.getStoreId())
        .orElseThrow(() -> new EntityNotFoundException(
            "사장 정보 없음 userId=" + confirmed.getStoreId()));

    // 3) 각 직원 배정 처리
    List<AssignStaffDateResponse> results = new ArrayList<>();
    for (Long staffId : staffIds) {
      // 3-1) Staff 조회 및 중복 확인
      Staff staff = staffRepository.findByStaffId(staffId)
          .orElseThrow(() -> new EntityNotFoundException("직원 없음 staffId=" + staffId));
      staffAssignmentRepository.findByEstimateNoAndStaffId(estimateNo, staffId)
          .ifPresent(a -> { throw new IllegalStateException("이미 배정된 직원입니다."); });

      // 3-2) 작업 날짜 파싱
      LocalDate workDate = LocalDate.parse(estimate.getMoveDate(), ESTIMATE_DATE_FORMAT);

      // 3-3) User 조회 및 이름 설정
      User user = userRepository.findById(staff.getUserId())
          .orElseThrow(() -> new EntityNotFoundException("유저 없음 id=" + staff.getUserId()));
      String staffName = user.getUserName();

      // 3-4) Assignment 생성 및 저장
      StaffAssignment assignment = StaffAssignment.builder()
          .estimateNo(estimateNo)
          .staffId(staffId)
          .staffName(staffName)
          .workDate(workDate)
          .build();
      staffAssignmentRepository.save(assignment);

      // 3-5) 응답 생성
      results.add(AssignStaffDateResponse.builder()
          .staffId(staffId)
          .staffName(staffName)
          .workDate(workDate)
          .build());
    }
    return results;
  }


  @Transactional(readOnly = true)
  public List<AvailableStaffResponse> getAvailableStaffList(Long estimateNo, Long userId) {

    Owner owner = ownerRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(OwnerErrorCode.OWNER_NOT_FOUND));

    Long ownerId = owner.getId();

    // 1. String moveDate → LocalDate 파싱
    String moveDateStr = estimateRepository.findById(estimateNo)
        .map(Estimate::getMoveDate)
        .orElseThrow(() -> new EntityNotFoundException("견적서가 없습니다 id=" + estimateNo));
    log.info("[1] estimateNo={} 의 moveDateStr={}", estimateNo, moveDateStr);

    LocalDate workDate = LocalDate.parse(moveDateStr, ESTIMATE_DATE_FORMAT);
    log.info("[2] 파싱된 workDate={}", workDate);

    // 2. 승인된 직원 전체
    List<Staff> approvedStaff = staffRepository.findByOwnerIdAndStatus(ownerId, Status.APPROVED);
    log.info("[3] ownerId={} 의 APPROVED staff 수={}", ownerId, approvedStaff.size());

    // 3. 휴가자 제외
    List<Staff> workingStaff = approvedStaff.stream()
        .filter(s -> !isOnTimeOff(s.getStaffId(), workDate))
        .toList();
    log.info("[4] 휴가 제외 후 workingStaff 수={}", workingStaff.size());

    // 4. 이미 배정된 직원 ID
    //    StaffAssignment.workDate를 String 그대로 쓰고 있다면
    //    repository에 findByWorkDate(String)로 선언하거나,
    //    workDate.format(DATE_FORMAT) 넘겨도 됩니다.
    List<Long> busyStaffIds = staffAssignmentRepository
        .findByWorkDate(workDate) // LocalDate 그대로 넘김
        .stream()
        .map(StaffAssignment::getStaffId)
        .toList();
    log.info("[5] workDateText='{}' 에 이미 배정된 staffId 목록={}", workDate, busyStaffIds);

    // 5. 최종 배정 가능 직원
    List<Staff> availableStaff = workingStaff.stream()
        .filter(s -> !busyStaffIds.contains(s.getStaffId()))
        .toList();
    log.info("[6] 최종 availableStaff 수={}", availableStaff.size());

    // 6. User 정보 매핑
    List<Long> userIds = availableStaff.stream()
        .map(Staff::getUserId)
        .toList();
    Map<Long, User> userMap = userRepository.findAllById(userIds)
        .stream().collect(Collectors.toMap(User::getId, Function.identity()));
    log.info("[7] userMap 키 목록={}", userMap.keySet());

    // 7. DTO 변환
    return availableStaff.stream()
        .map(s -> {
          User u = userMap.get(s.getUserId());
          return AvailableStaffResponse.builder()
              .staffId(s.getStaffId())
              .staffName(u != null ? u.getUserName() : "이름 없음")
              .staffPhoneNum(u != null ? u.getPhoneNumber() : "번호 없음")
              .status("AVAILABLE")
              .build();
        })
        .toList();
  }

  // staffId 가 workDate에 휴가(승인된 TimeOff) 중인지 체크
  private boolean isOnTimeOff(Long staffId, LocalDate workDate) {
    return staffTimeOffepository.existsByStaffIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        staffId,
        Status.APPROVED,
        workDate,
        workDate
    );
  }

  // 새로 추가
  @Transactional(readOnly = true)
  public AssignedStaffListResponse getAssignedStaffList(Long estimateNo, Long userId) {
    // 0) 로그인한 userId -> Owner 조회
    Owner owner = ownerRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException(
            "사장 정보가 없습니다. userId=" + userId));
    Long ownerId = owner.getId();

    // 1) CONFIRMED된 estimate_owner_response 조회
    EstimateOwnerResponse confirmed = estimateOwnerResponseRepository
        .findByEstimateNoAndStatus(estimateNo, EstimateStatus.CONFIRMED)
        .orElseThrow(() -> new EntityNotFoundException(
            "확정된 견적이 없습니다. estimateNo=" + estimateNo));

    // 2) 권한 검증: confirmed.storeId와 ownerId 비교
    if (!confirmed.getStoreId().equals(ownerId)) {
      throw new CustomException(EstimateErrorCode.ESTIMATE_STORE_MISMATCH);
    }

    // 3) estimateNo로 배정 내역 조회
    List<StaffAssignment> assignments =
        staffAssignmentRepository.findByEstimateNo(estimateNo);

    // 4) 개수 계산
    long count = assignments.size();

    // 5) DTO 변환
    List<StaffAssignmentResponse> staffList = assignments.stream()
        .map(sa -> StaffAssignmentResponse.builder()
            .staffId(sa.getStaffId())
            .staffName(sa.getStaffName())
            .workDate(sa.getWorkDate())
            .estimateNo(sa.getEstimateNo())
            .build())
        .toList();

    // 6) 응답 반환
    return AssignedStaffListResponse.builder()
        .count(count)
        .staffList(staffList)
        .build();
  }
}
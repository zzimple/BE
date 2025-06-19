package com.zzimple.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.owner.dto.response.AvailableStaffResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.entity.StaffAssignment;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.repository.StaffAssignmentRepository;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.staff.repository.StaffTimeOffepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter ESTIMATE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");
  private final OwnerRepository ownerRepository;

  @Transactional
  public AssignStaffDateResponse assignWithDate(Long estimateNo, Long staffId) {
    log.info("[직원 배정 시작] estimateNo={}, staffId={}", estimateNo, staffId);

    // 1) Staff 조회
    Staff staff = staffRepository.findByStaffId(staffId)
        .orElseThrow(() -> new EntityNotFoundException("직원 없음 staffId=" + staffId));

    // 2) Estimate 조회
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적 없음 id=" + estimateNo));

    // 3) User 조회
    User user = userRepository.findById(staff.getUserId())
        .orElseThrow(() -> new EntityNotFoundException("유저 없음 id=" + staff.getUserId()));
    String staffName = user.getUserName();

    // 4) 중복 배정 확인
    staffAssignmentRepository.findByEstimateNoAndStaffId(estimateNo, staffId)
        .ifPresent(a -> { throw new IllegalStateException("이미 배정된 직원입니다."); });

    // 5) 날짜 포맷 변환
    LocalDate parsed = LocalDate.parse(estimate.getMoveDate(), ESTIMATE_DATE_FORMAT);
    String workDate = parsed.format(DATE_FORMAT);
    log.info("[포맷된 작업 날짜] {}", workDate);

    // 6) 엔티티 생성 및 저장
    StaffAssignment assignment = StaffAssignment.builder()
        .estimateNo(estimateNo)
        .staffId(staffId)
        .staffName(staffName)
        .workDate(workDate)
        .build();
    staffAssignmentRepository.save(assignment);

    log.info("[직원 배정 완료] staffId={}, staffName={}, workDate={}",
        staffId, staffName, workDate);

    // 7) 응답 반환
    return AssignStaffDateResponse.builder()
        .staffId(staffId)
        .staffName(staffName)
        .workDate(workDate)
        .build();
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
    String workDateText = workDate.format(DATE_FORMAT);
    List<Long> busyStaffIds = staffAssignmentRepository
        .findByWorkDate(workDateText)
        .stream()
        .map(StaffAssignment::getStaffId)
        .toList();
    log.info("[5] workDateText='{}' 에 이미 배정된 staffId 목록={}", workDateText, busyStaffIds);

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
}
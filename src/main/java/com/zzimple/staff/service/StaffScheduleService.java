package com.zzimple.staff.service;

import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.dto.request.StaffTimeOffRequest;
import com.zzimple.staff.dto.response.StaffAssignmentResponse;
import com.zzimple.staff.dto.response.StaffScheduleCalendarItem;
import com.zzimple.staff.dto.response.StaffTimeOffResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.entity.StaffAssignment;
import com.zzimple.staff.entity.StaffTimeOff;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.exception.StaffErrorCode;
import com.zzimple.staff.repository.StaffAssignmentRepository;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.staff.repository.StaffTimeOffepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
public class StaffScheduleService {

  private final StaffTimeOffepository staffTimeOffepository;
  private final StaffRepository staffRepository;
  private final StoreRepository storeRepository;
  private final UserRepository userRepository;
  private final StaffAssignmentRepository staffAssignmentRepository;
  private final EstimateRepository estimateRepository;

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

  // 2) 사장님 승인/거절
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
      log.info("[휴무 승인] requestId={}, staffId={} added to calendar", reqeust_staffId,
          reqeust_staffId.getStaffName());
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

  public List<StaffScheduleCalendarItem> getMyMonthlyCalendar(Long userId, String yearMonthStr) {
    Staff staff = staffRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(StaffErrorCode.INVALID_STAFF_ROLE));

    Long staffId = staff.getStaffId();
    Long storeId = staff.getStoreId();

    YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
    LocalDate start = yearMonth.atDay(1);
    LocalDate end = yearMonth.atEndOfMonth();

    // 1. 근무 일정 조회
    List<StaffAssignment> assignments = staffAssignmentRepository
        .findAllByStaffIdAndWorkDateBetween(staffId, start, end);

    // 2. 휴무 일정 조회
    List<StaffTimeOff> timeOffs = staffTimeOffepository
        .findByStaffIdAndStatusAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
            staffId, Status.APPROVED, start, end
        );

    // 3. 근무 일정 변환
    List<StaffScheduleCalendarItem> workItems = assignments.stream()
        .map(a -> {
          Estimate estimate = estimateRepository.findByEstimateNo(a.getEstimateNo())
              .orElseThrow(() -> new EntityNotFoundException("Estimate not found"));

          return StaffScheduleCalendarItem.builder()
              .date(a.getWorkDate())
              .type("WORK")
              .work(StaffScheduleCalendarItem.WorkInfo.builder()
                  .estimateNo(a.getEstimateNo())
                  .storeId(storeId)
                  .staffName(a.getStaffName())
                  .fromAddress(estimate.getFromAddress().getRoadFullAddr())
                  .toAddress(estimate.getToAddress().getRoadFullAddr())
                  .build())
              .build();
        })
        .toList();


    // 4. 휴무 일정 변환 (start ~ end 범위 내 날짜별로 쪼개기)
    List<StaffScheduleCalendarItem> timeOffItems = timeOffs.stream()
        .flatMap(to -> {
          LocalDate from = to.getStartDate().isBefore(start) ? start : to.getStartDate();
          LocalDate toDate = to.getEndDate().isAfter(end) ? end : to.getEndDate();
          return from.datesUntil(toDate.plusDays(1)).map(date ->
              StaffScheduleCalendarItem.builder()
                  .date(date)
                  .type("TIME_OFF")
                  .timeOff(StaffScheduleCalendarItem.TimeOffInfo.builder()
                      .type(to.getType())
                      .reason(to.getReason())
                      .build())
                  .build()
          );
        })
        .toList();

    // 5. 병합 후 날짜 기준 정렬
    return new java.util.ArrayList<StaffScheduleCalendarItem>() {{
      addAll(workItems);
      addAll(timeOffItems);
      sort(java.util.Comparator.comparing(StaffScheduleCalendarItem::getDate));
    }};
  }
}
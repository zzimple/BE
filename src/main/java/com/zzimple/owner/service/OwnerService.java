package com.zzimple.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.EstimateOwnerResponse;
import com.zzimple.estimate.owner.repository.EstimateCalculationRepository;
import com.zzimple.estimate.owner.repository.EstimateOwnerResponseRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.owner.dto.response.MonthlySalesAvgResponse;
import com.zzimple.owner.dto.response.MonthlySalesItemResponse;
import com.zzimple.owner.dto.response.OwnerProfileResponse;
import com.zzimple.owner.dto.response.WeeklySalesSimpleResponse;
import com.zzimple.owner.exception.BusinessErrorCode;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.global.sms.service.SmsService;
import com.zzimple.owner.dto.request.OwnerLoginIdCheckRequest;
import com.zzimple.owner.dto.request.OwnerSignUpRequest;
import com.zzimple.owner.dto.response.OwnerLoginIdCheckResponse;
import com.zzimple.owner.dto.response.OwnerSignUpResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.redis.BusinessRedisRepository;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerService {

  private final OwnerRepository ownerRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final BusinessRedisRepository businessRedisRepository;
  private final StoreRepository storeRepository;
  private final EstimateCalculationRepository estimateCalculationRepository;
  private final EstimateRepository estimateRepository;
  private final EstimateOwnerResponseRepository estimateOwnerResponseRepository;

  private final SmsService smsService;

  public OwnerLoginIdCheckResponse checkLoginIdDuplicate(OwnerLoginIdCheckRequest request) {
    // 로그인 아이디(이메일) 존재 여부 확인
    boolean isDuplicate = ownerRepository.findByBusinessNumber(request.getLoginId()).isPresent();

    // 응답 생성
    return OwnerLoginIdCheckResponse.builder()
        .isDuplicate(isDuplicate)
        .build();
  }

  @Transactional
  public OwnerSignUpResponse registerOwner(OwnerSignUpRequest request) {

    // 0. 휴대폰 인증 검사
    smsService.verifyPhoneCertified(request.getPhoneNumber());

    // 1. 중복 아이디
    if (ownerRepository.findByBusinessNumber(request.getB_no()).isPresent()) {
      log.warn("[회원가입 실패] 이미 존재하는 아이디 - ID: {}", request.getB_no());
      throw new CustomException(BusinessErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
    }

    // 2. 인증된 사업자 번호 Redis에서 삭제
    businessRedisRepository.deleteById(request.getB_no());
    log.info("[회원가입] 사업자번호 인증정보 삭제 완료 - b_no: {}", request.getB_no());

    try {
      // 3. 비밀번호 암호화
      String encodedPassword = passwordEncoder.encode(request.getPassword());

      // 4. User 엔티티 생성 (role=OWNER 로 고정)
      User base = User.builder()
          .loginId(request.getB_no())
          .password(encodedPassword)
          .userName(request.getUserName())
          .phoneNumber(request.getPhoneNumber())
          .email(request.getEmail())
          .role(UserRole.OWNER)
          .build();
      userRepository.save(base);

      // 5. Owner 저장
      Owner owner = Owner.builder()
          .userId(base.getId())
          .businessNumber(request.getB_no())
          .insured(request.getInsured())
          .status("계속사업자")
          .roadFullAddr(request.getRoadFullAddr())
          .roadAddrPart1(request.getRoadAddrPart1())
          .addrDetail(request.getAddrDetail())
          .zipNo(request.getZipNo())
          .build();
      ownerRepository.save(owner);

      // 6. Store 저장 - 가게 이름과 전체 도로명 주소 저장
      Store store = Store.builder()
          .ownerId(owner.getId())
          .name(request.getStoreName())
          .address(request.getRoadFullAddr())
          .build();
      storeRepository.save(store);

      log.info("[회원가입] 회원가입 및 가게 등록 성공 - 사업자번호: {}, 이름: {}, 가게: {}",
          base.getLoginId(), base.getUserName(), store.getName());

      return OwnerSignUpResponse.builder()
          .isSuccess(true)
          .build();
    } catch (Exception e) {
      log.error("[회원가입] 회원가입 처리 중 예외 발생 - ID: {}", request.getB_no(), e);
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 사장님 프로필 조회
  public OwnerProfileResponse getOwnerProfile(Long userId) {

    Owner owner = ownerRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("Owner not found"));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new RuntimeException("Store not found"));

    return new OwnerProfileResponse(user.getUserName(), user.getLoginId(), user.getEmail(), owner.getRoadFullAddr(), owner.getRoadAddrPart1(), owner.getAddrDetail(), owner.getZipNo(), store.getId());
  }

  // 매출 메소드
  public List<MonthlySalesAvgResponse> getMonthlyAverageSales(Long userId) {
    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new RuntimeException("Store not found"));
    Long storeId = store.getId();

    // CONFIRMED 상태 estimateNo
    List<Long> confirmedEstimateNos = estimateOwnerResponseRepository.findByStatus(EstimateStatus.CONFIRMED).stream()
        .map(EstimateOwnerResponse::getEstimateNo)
        .toList();

    // 해당 사장님의 계산 정보 중 CONFIRMED된 것만 필터링
    List<EstimateCalculation> calcs = estimateCalculationRepository.findByStoreId(storeId).stream()
        .filter(c -> confirmedEstimateNos.contains(c.getEstimateNo()))
        .toList();

    List<Long> estimateNo = calcs.stream()
        .map(EstimateCalculation::getEstimateNo)
        .distinct()
        .toList();

    DateTimeFormatter rawFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    List<Estimate> estimates = estimateRepository.findAllById(estimateNo);
    Map<Long, Estimate> estimateMap = estimates.stream()
        .collect(Collectors.toMap(Estimate::getEstimateNo, Function.identity()));

// ✅ 수정된 코드: estimateNo 중 status가 CONFIRMED인 응답만 필터링하여 Map으로 저장
    Map<Long, EstimateOwnerResponse> responseMap = estimateOwnerResponseRepository
        .findByEstimateNoInAndStatus(estimateNo, EstimateStatus.CONFIRMED).stream()
        .collect(Collectors.toMap(EstimateOwnerResponse::getEstimateNo, Function.identity()));

    Map<Long, User> userMap = userRepository.findAll().stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

    // 월별 그룹화
    Map<String, List<MonthlySalesItemResponse>> grouped = new HashMap<>();

    for (EstimateCalculation ec : calcs) {
      Estimate est = estimateMap.get(ec.getEstimateNo());
      if (est == null || est.getMoveDate() == null) continue;

      LocalDate moveDate = LocalDate.parse(est.getMoveDate(), rawFormatter);
      String yearMonth = moveDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

      String displayDate = moveDate.format(displayFormatter);
      User customer = userMap.get(est.getUserId());
      String customerName = (customer != null) ? customer.getUserName() : "알 수 없음";
      String status = responseMap.get(ec.getEstimateNo()) != null
          ? responseMap.get(ec.getEstimateNo()).getStatus().name()
          : "UNKNOWN";

      MonthlySalesItemResponse item = new MonthlySalesItemResponse(
          ec.getId(),
          displayDate,
          customerName,
          ec.getFinalTotalPrice(),
          status
      );

      grouped.computeIfAbsent(yearMonth, k -> new ArrayList<>()).add(item);
    }

    // 평균과 함께 DTO로 변환
    return grouped.entrySet().stream()
        .map(entry -> {
          String month = entry.getKey();
          List<MonthlySalesItemResponse> items = entry.getValue();
          double avg = items.stream().mapToInt(MonthlySalesItemResponse::getAmount).average().orElse(0.0);
          return new MonthlySalesAvgResponse(month, avg, items);
        })
        .sorted(Comparator.comparing(MonthlySalesAvgResponse::getMonth))
        .collect(Collectors.toList());
  }

  // 주간 매출 조회 (평균 매출 기준)
  public List<WeeklySalesSimpleResponse> getWeeklySales(Long userId) {
    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new RuntimeException("Store not found"));
    Long storeId = store.getId();

    List<Long> confirmedEstimateNos = estimateOwnerResponseRepository.findByStatus(EstimateStatus.CONFIRMED).stream()
        .map(EstimateOwnerResponse::getEstimateNo)
        .toList();

    List<EstimateCalculation> calcs = estimateCalculationRepository.findByStoreId(storeId).stream()
        .filter(c -> confirmedEstimateNos.contains(c.getEstimateNo()))
        .toList();

    List<Long> estimateNos = calcs.stream()
        .map(EstimateCalculation::getEstimateNo)
        .distinct()
        .toList();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    List<Estimate> estimates = estimateRepository.findAllById(estimateNos);
    Map<Long, LocalDate> moveDateMap = estimates.stream()
        .filter(e -> e.getMoveDate() != null)
        .collect(Collectors.toMap(
            Estimate::getEstimateNo,
            e -> LocalDate.parse(e.getMoveDate(), dateFormatter)
        ));

    // ✅ 주차별 (월요일 기준) -> 금액 리스트로 그룹핑 (평균 계산용)
    Map<LocalDate, List<Integer>> weeklyGroup = new HashMap<>();

    for (EstimateCalculation ec : calcs) {
      LocalDate moveDate = moveDateMap.get(ec.getEstimateNo());
      if (moveDate == null) continue;

      LocalDate weekStart = moveDate.with(DayOfWeek.MONDAY);
      weeklyGroup.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(ec.getFinalTotalPrice());
    }

    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    return weeklyGroup.entrySet().stream()
        .map(entry -> {
          String week = entry.getKey().format(outputFormatter);
          List<Integer> prices = entry.getValue();
          int avg = (int) prices.stream().mapToInt(Integer::intValue).average().orElse(0.0);
          return new WeeklySalesSimpleResponse(week, avg);
        })
        .sorted(Comparator.comparing(WeeklySalesSimpleResponse::getWeekStartDate))
        .collect(Collectors.toList());
  }
}
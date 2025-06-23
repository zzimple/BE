package com.zzimple.estimate.guest.service;

import com.zzimple.estimate.guest.dto.EstimateResponseList;
import com.zzimple.estimate.guest.dto.MyEstimatePreview;
import com.zzimple.estimate.guest.dto.response.EstimateResponsePreview;
import com.zzimple.estimate.guest.dto.response.PagedMyEstimates;
import com.zzimple.estimate.owner.entity.EstimateOwnerResponse;
import com.zzimple.estimate.owner.entity.EstimateResponse;
import com.zzimple.estimate.owner.entity.MoveItemPriceByStore;
import com.zzimple.estimate.owner.repository.EstimateOwnerResponseRepository;
import com.zzimple.estimate.owner.repository.EstimateResponseRepository;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.MoveItemPriceByStoreRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.store.exception.StoreErrorCode;
import org.springframework.security.access.AccessDeniedException;
import com.zzimple.estimate.guest.dto.response.EstimateListDetailResponse;
import com.zzimple.estimate.guest.dto.response.EstimateListResponse;
import com.zzimple.estimate.guest.dto.response.GuestEstimateRespondResult;
import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest.ExtraChargeRequest;
import com.zzimple.estimate.owner.dto.response.MoveItemPreviewDetailResponse;
import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.entity.StorePriceSetting;
import com.zzimple.estimate.owner.repository.EstimateCalculationRepository;
import com.zzimple.estimate.owner.repository.EstimateExtraChargeRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
import com.zzimple.estimate.owner.repository.StorePriceSettingRepository;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestEstimateService {

  private final EstimateRepository estimateRepository;
  private final MoveItemsRepository moveItemsRepository;
  private final EstimateExtraChargeRepository estimateExtraChargeRepository;
  private final MoveItemExtraChargeRepository moveItemExtraChargeRepository;
  private final StoreRepository storeRepository;
  private final OwnerRepository ownerRepository;
  private final UserRepository userRepository;
  private final EstimateCalculationRepository estimateCalculationRepository;
  private final StorePriceSettingRepository storePriceSettingRepository;
  private final EstimateResponseRepository estimateResponseRepository;
  private final EstimateOwnerResponseRepository estimateOwnerResponseRepository;

  public PagedResponse<EstimateListResponse> getAcceptedEstimatesForUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Estimate> estimates = estimateRepository.findByUserIdAndStatus(userId, EstimateStatus.ACCEPTED, pageable);

    log.info("[견적 조회] 유저 ID: {}, 페이지: {}, 총 요소 수: {}", userId, page, estimates.getTotalElements());

    Page<EstimateListResponse> mappedPage = estimates.map(estimate -> {
      // 1) storeName 조회
      String storeName = "알 수 없음";
      if (estimate.getStoreId() != null) {
        storeName = storeRepository.findById(estimate.getStoreId())
            .map(store -> {
              String name = store.getName();
              return (name != null ? name : "알 수 없음");
            })
            .orElseGet(() -> {
              log.warn("[Store 조회 실패] storeId={}에 해당하는 Store 없음", estimate.getStoreId());
              return "알 수 없음";
            });
      }

      // 2) truckCount: null-safe
      Integer truckCount = estimate.getTruckCount();
      if (truckCount == null) truckCount = 0;

      // 3) totalPrice: Long → Integer 변환
      Integer totalPrice = estimateCalculationRepository.findByEstimateNo(estimate.getEstimateNo())
          .map(EstimateCalculation::getFinalTotalPrice)
          .map(price -> {
            try {
              return Math.toIntExact(price); // ✅ 안전하게 변환
            } catch (ArithmeticException e) {
              log.warn("[총가격 변환 오류] 값이 Integer 범위를 초과했습니다. estimateNo={}, price={}", estimate.getEstimateNo(), price);
              return Integer.MAX_VALUE; // 혹은 다른 fallback 처리
            }
          })
          .orElseGet(() -> {
            Integer fallback = estimate.getTotalPrice();
            return fallback != null ? fallback : 0;
          });

      return EstimateListResponse.builder()
          .estimateNo(estimate.getEstimateNo())
          .storeName(storeName)
          .truckCount(truckCount)
          .totalPrice(totalPrice)
          .build();
    });

    return PagedResponse.<EstimateListResponse>builder()
        .content(mappedPage.getContent())
        .page(mappedPage.getNumber())
        .size(mappedPage.getSize())
        .totalElements(mappedPage.getTotalElements())
        .totalPages(mappedPage.getTotalPages())
        .last(mappedPage.isLast())
        .build();
  }

  private String formatMoveDate(String moveDateInt) {
    LocalDate date = LocalDate.parse(String.valueOf(moveDateInt), DateTimeFormatter.BASIC_ISO_DATE);
    String dayOfWeekKor = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
    return date.getMonthValue() + "/" + date.getDayOfMonth() + " (" + dayOfWeekKor + ")";
  }

  private String formatMoveTime(LocalDateTime scheduledAt) {
    int hour = scheduledAt.getHour();
    int minute = scheduledAt.getMinute();
    String ampm = hour < 12 ? "오전" : "오후";
    int displayHour = hour % 12 == 0 ? 12 : hour % 12;
    return ampm + " " + displayHour + ":" + String.format("%02d", minute);
  }

  // 최종 견적서 상세보기
  @Transactional(readOnly = true)
  public EstimateListDetailResponse getEstimateDetail(Long estimateNo, Long storeId) {
    log.info("[최종 견적 상세조회] estimateNo = {}", estimateNo);

    Estimate estimate = estimateRepository.findByEstimateNo(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    // 2) 이 매장에 대한 사장님 응답이 있는지
    EstimateOwnerResponse ownerResp = estimateOwnerResponseRepository
        .findByEstimateNoAndStoreId(estimateNo, storeId)
        .orElseThrow(() -> new EntityNotFoundException(
            "응답 없음 estimateNo=" + estimateNo + ", storeId=" + storeId));

    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new EntityNotFoundException("Store not found for id=" + storeId));

    log.info("▶ getEstimateDetail 호출: estimateNo={} storeId={}",
        estimateNo, storeId);


    // StorePriceSetting 조회
    StorePriceSetting setting = storePriceSettingRepository.findById(store.getId())
        .orElseThrow(() -> new EntityNotFoundException("StorePriceSetting not found for storeId=" + store.getId()));

    Owner owner = ownerRepository.findById(store.getOwnerId())
        .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

    User user = userRepository.findById(owner.getUserId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    EstimateCalculation calculation = estimateCalculationRepository
        .findByEstimateNoAndStoreId(estimateNo, storeId)   // ← 수정
        .orElseThrow(() -> new EntityNotFoundException(
            "계산 정보가 없습니다. estimateNo=" + estimateNo + ", storeId=" + storeId));  // ← 수정

    // 짐 목록 조회
    List<MoveItems> moveItems = moveItemsRepository.findByEstimateNo(estimateNo);
    log.info("[짐 목록 조회] estimateNo = {}, 항목 수 = {}", estimateNo, moveItems.size());

    // 물품별 단가 및 추가금 구성
    List<SaveEstimatePriceRequest> itemPriceDetails = moveItems.stream()
        .map(item -> {
          List<MoveItemExtraCharge> extraList =
              moveItemExtraChargeRepository.findByEstimateNoAndStoreIdAndItemTypeId(
                  estimateNo, storeId, item.getItemTypeId());
          log.info("[물품 추가금] itemId = {}, 추가금 개수 = {}", item.getItemTypeId(), extraList.size());

          List<ExtraChargeRequest> extraChargeRequests = extraList.stream()
              .map(extra -> {
                ExtraChargeRequest dto = new ExtraChargeRequest();
                dto.setAmount(extra.getAmount());
                dto.setReason(extra.getReason());
                return dto;
              }).toList();

//          int basePrice = moveItemPriceByStoreRepository
//              .findByStoreIdAndItemTypeId(estimate.getStoreId(), item.getItemTypeId())
//              .map(MoveItemPriceByStore::getBasePrice)
//              .orElse(0);
//
//          return SaveEstimatePriceRequest.builder()
//              .itemTypeId(item.getItemTypeId())
//              .itemTypeName(item.getItemTypeName())
//              .quantity(item.getQuantity())
//              .basePrice(basePrice)
//              .extraCharges(extraChargeRequests)
//              .build();
//        })
//        .toList();

          SaveEstimatePriceRequest priceRequest = new SaveEstimatePriceRequest();
          priceRequest.setItemTypeId(item.getItemTypeId());
          priceRequest.setItemTypeName(item.getItemTypeName());
          priceRequest.setQuantity(item.getQuantity());
          priceRequest.setBasePrice(item.getPer_item_totalPrice()); // 또는 basePrice 별도로
          priceRequest.setExtraCharges(extraChargeRequests);

          return priceRequest;
        }).toList();

    // 기타 추가금 (트럭, 공휴일 등)
    List<SaveEstimatePriceRequest.ExtraChargeRequest> extraCharges =
        estimateExtraChargeRepository.findByEstimateNoAndStoreId(estimateNo, storeId).stream()
            .map(extra -> {
              SaveEstimatePriceRequest.ExtraChargeRequest dto = new SaveEstimatePriceRequest.ExtraChargeRequest();
              dto.setAmount(extra.getAmount());
              dto.setReason(extra.getReason());
              return dto;
            }).toList();

    log.info("[기타 추가금 조회] estimateNo = {}, 개수 = {}", estimateNo, extraCharges.size());

    log.info("[견적 총합] estimateNo = {}, totalPrice = {}", estimateNo, estimate.getTotalPrice());

    return EstimateListDetailResponse.builder()
        .estimateNo(estimate.getEstimateNo())
        .storeName(store.getName())
        .ownerName(user.getUserName())
        .ownerPhone(user.getPhoneNumber())
        .userId(estimate.getUserId())
        .moveDate(estimate.getMoveDate())
        .moveTime(estimate.getMoveTime())
        .moveType(estimate.getMoveType())
        .optionType(estimate.getOptionType())
        .fromAddress(estimate.getFromAddress())
        .fromDetailInfo(estimate.getFromDetail())
        .toAddress(estimate.getToAddress())
        .toDetailInfo(estimate.getToDetail())
        .customerMemo(estimate.getCustomerMemo())
        .truckCount(estimate.getTruckCount())
        .truckTotalPrice(setting.getPerTruckCharge() * estimate.getTruckCount())
        .ownerMessage(estimate.getOwnerMessage())
        .itemPriceDetails(itemPriceDetails)
        .extraCharges(extraCharges)
        .items(moveItems.stream()
            .map(MoveItemPreviewDetailResponse::from) // 예시 변환 메서드
            .toList())
        .totalPrice(calculation.getFinalTotalPrice())
        .holidayCharge(estimate.getIsHoliday() ? setting.getHolidayCharge() : null)
        .goodDayCharge(estimate.getIsGoodDay() ? setting.getGoodDayCharge() : null)
        .weekendCharge(estimate.getIsWeekend() ? setting.getWeekendCharge() : null)

        .build();
  }

  @Transactional(readOnly = true)
  public PagedMyEstimates getMyEstimates(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Estimate> pageResult = estimateRepository.findByUserId(userId, pageable);

    List<MyEstimatePreview> previews = pageResult.getContent().stream()
        .map(e -> {
          int responseCount = estimateOwnerResponseRepository.countByEstimateNo(e.getEstimateNo());
          return MyEstimatePreview.builder()
              .estimateNo(e.getEstimateNo())
              .moveDate(e.getMoveDate())
              .fromAddr(e.getFromAddress().getRoadFullAddr())
              .toAddr(e.getToAddress().getRoadFullAddr())
              .createdAt(e.getCreatedAt())
              .responseCount(responseCount)
              .build();
        })
        .toList();

    return new PagedMyEstimates(previews, page, size, pageResult.getTotalElements(), pageResult.getTotalPages());
  }

  @Transactional(readOnly = true)
  public EstimateResponseList getResponses(Long estimateNo, Long userId) {
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new CustomException(EstimateErrorCode.ESTIMATE_NOT_FOUND));

    if (!estimate.getUserId().equals(userId)) {
      throw new CustomException(EstimateErrorCode.ESTIMATE_ACCESS_DENIED);
    }

    List<EstimateOwnerResponse> responses = estimateOwnerResponseRepository.findByEstimateNo(estimateNo);

    List<EstimateResponsePreview> previews = responses.stream()
        .map(r -> {
          Long storeId = r.getStoreId();
          Store store = storeRepository.findById(storeId)
              .orElse(null);

          EstimateCalculation calc = estimateCalculationRepository
              .findByEstimateNoAndStoreId(estimateNo, storeId)
              .orElse(null);

          return EstimateResponsePreview.builder()
              .storeId(storeId)
              .storeName(store != null ? store.getName() : "알 수 없음")
              .truckCount(r.getTruckCount())
              .ownerMessage(r.getOwnerMessage())
              .status(r.getStatus())
              .respondedAt(r.getRespondedAt())
              .itemsTotal(calc != null ? calc.getItemsTotalPrice() : 0)
              .extraTotal(calc != null ? calc.getExtraChargesTotal() : 0)
              .finalTotal(calc != null ? calc.getFinalTotalPrice() : 0)
              .build();
        })
        .toList();

    return new EstimateResponseList(estimateNo, previews);
  }

  // 손님 최종 응답
  public GuestEstimateRespondResult respondToEstimate(Long estimateNo, Long selectedStoreId, Long userId) {

    Estimate estimate = estimateRepository.findByEstimateNo(estimateNo)
        .orElseThrow(() -> new CustomException(EstimateErrorCode.ESTIMATE_NOT_FOUND));

    if (!estimate.getUserId().equals(userId)) {
      throw new AccessDeniedException("본인의 견적서만 응답할 수 있습니다.");
    }

//    Store store = storeRepository.findById(selectedStoreId)
//        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // ✅ 모든 EstimateResponse 중에서
    List<EstimateOwnerResponse> responses = estimateOwnerResponseRepository.findByEstimateNo(estimateNo);

    if (responses.isEmpty()) {
      throw new CustomException(EstimateErrorCode.NO_RESPONSES_FOUND);
    }

    boolean confirmedOne = false;

    for (EstimateOwnerResponse r : responses) {
      if (r.getStoreId().equals(selectedStoreId)) {
        r.setStatus(EstimateStatus.CONFIRMED); // ✅ 선택된 사장님
        confirmedOne = true;
      } else {
        r.setStatus(EstimateStatus.REJECTED);  // ❌ 나머지 사장님
      }
    }

    if (!confirmedOne) {
      throw new CustomException(EstimateErrorCode.STORE_RESPONSE_NOT_FOUND);
    }

    estimate.setStoreId(selectedStoreId);
    estimate.setStatus(EstimateStatus.CONFIRMED); // 전체 견적 상태도 확정
    estimateRepository.save(estimate);
    estimateOwnerResponseRepository.saveAll(responses);

    return GuestEstimateRespondResult.builder()
        .estimateNo(estimateNo)
        .confirmedStoreId(selectedStoreId)
        .status(EstimateStatus.CONFIRMED)
        .build();
  }
}


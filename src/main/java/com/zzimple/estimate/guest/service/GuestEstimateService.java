package com.zzimple.estimate.guest.service;

import com.zzimple.estimate.guest.dto.response.EstimateListDetailResponse;
import com.zzimple.estimate.guest.dto.response.EstimateListResponse;
import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.enums.MoveItemCategory;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest.ExtraChargeRequest;
import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.repository.EstimateCalculationRepository;
import com.zzimple.estimate.owner.repository.EstimateExtraChargeRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
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

  // 사장님에게 견적서 요청 온거 미리보기
  public PagedResponse<EstimateListResponse> getAcceptedEstimatesForUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")); // createdAt 기준 정렬
    Page<Estimate> estimates = estimateRepository.findByUserIdAndStatus(userId, EstimateStatus.ACCEPTED, pageable);

    log.info("[견적 조회] 유저 ID: {}, 페이지: {}, 조회된 수: {}", userId, page, estimates.getTotalElements());

    Page<EstimateListResponse> mappedPage = estimates.map(estimate -> {
      String moveDate = formatMoveDate(estimate.getMoveDate());
      String moveTime = formatMoveTime(estimate.getMoveTime());

      List<MoveItems> items = moveItemsRepository.findByEstimateNo(estimate.getEstimateNo());
      long furnitureCount = items.stream().filter(i -> i.getCategory() == MoveItemCategory.FURNITURE).count();
      long applianceCount = items.stream().filter(i -> i.getCategory() == MoveItemCategory.APPLIANCE).count();

      return EstimateListResponse.builder()
          .estimateNo(estimate.getEstimateNo())
          .moveDate(moveDate)
          .moveTime(moveTime)
          .fromAddress(estimate.getFromAddress())
          .toAddress(estimate.getToAddress())
          .furnitureCount((int) furnitureCount)
          .applianceCount((int) applianceCount)
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

  // 사장님한테 온 견적서 상세보기
  @Transactional(readOnly = true)
  public EstimateListDetailResponse getEstimateDetail(Long estimateNo) {
    log.info("[견적 상세조회] estimateNo = {}", estimateNo);

    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    Store store = storeRepository.findById(estimate.getStoreId())
        .orElseThrow(() -> new EntityNotFoundException("Store not found"));

    Owner owner = ownerRepository.findById(store.getOwnerId())
        .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

    User user = userRepository.findById(owner.getUserId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    EstimateCalculation calculation = estimateCalculationRepository.findByEstimateNo(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("Estimate calculation not found"));

    // 짐 목록 조회
    List<MoveItems> moveItems = moveItemsRepository.findByEstimateNo(estimateNo);
    log.info("[짐 목록 조회] estimateNo = {}, 항목 수 = {}", estimateNo, moveItems.size());

    // 물품별 단가 및 추가금 구성
    List<SaveEstimatePriceRequest> itemPriceDetails = moveItems.stream()
        .map(item -> {
          List<MoveItemExtraCharge> extraList = moveItemExtraChargeRepository.findByItemTypeId(item.getItemTypeId());
          log.info("[물품 추가금] itemId = {}, 추가금 개수 = {}", item.getItemTypeId(), extraList.size());

          List<ExtraChargeRequest> extraChargeRequests = extraList.stream()
              .map(extra -> {
                ExtraChargeRequest dto = new ExtraChargeRequest();
                dto.setAmount(extra.getAmount());
                dto.setReason(extra.getReason());
                return dto;
              }).toList();

          return SaveEstimatePriceRequest.builder()
              .itemTypeId(item.getItemTypeId())
              .quantity(item.getQuantity())
              .basePrice(item.getBasePrice())
              .extraCharges(extraChargeRequests)
              .build();
        })
        .toList();

    // 기타 추가금 (트럭, 공휴일 등)
    List<SaveEstimatePriceRequest.ExtraChargeRequest> extraCharges =
        estimateExtraChargeRepository.findByEstimateNo(estimateNo).stream()
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
        .storeName(estimate.getStoreName())
        .ownerName(user.getUserName())
        .ownerPhone(user.getPhoneNumber())
        .userId(estimate.getUserId())
        .moveDate(estimate.getMoveDate())
        .moveType(estimate.getMoveType())
        .optionType(estimate.getOptionType())
        .fromAddress(estimate.getFromAddress())
        .fromDetailInfo(estimate.getFromDetail())
        .toAddress(estimate.getToAddress())
        .toDetailInfo(estimate.getToDetail())
        .customerMemo(estimate.getCustomerMemo())
        .truckCount(estimate.getTruckCount())
        .ownerMessage(estimate.getOwnerMessage())
        .itemPriceDetails(itemPriceDetails)
        .extraCharges(extraCharges)
        .totalPrice(calculation.getFinalTotalPrice())

        .build();
  }
}

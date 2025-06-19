package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.dto.response.EstimatePreviewResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.exception.StoreErrorCode;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.exception.UserErrorCode;
import com.zzimple.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimatePreviewService {

  private final EstimateRepository estimateRepository;
  private final StoreRepository storeRepository;
  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;

  // 공개 견적서 페이징
  public Page<EstimatePreviewResponse> getEstimatePreview(
      Long userId,
      int page,
      int size,
      Integer moveYear,
      Integer moveMonth,
      Integer moveDay,
      String moveType,
      String moveOption,
      String fromRegion1,
      String fromRegion2,
      String toRegion1,
      String toRegion2,
      EstimateStatus status
  ) {
    log.info("[EstimatePublicService] 공개 견적서 조회 시작 - ownerId={}, page={}, size={}, moveYear={}, moveMonth={}, moveDay={}, moveType={}, fromRegion1={}, fromRegion2={}, toRegion1={}, toRegion2={}"
        , userId, page, size, moveYear, moveMonth, moveDay, moveType, fromRegion1, fromRegion2, toRegion1, toRegion2);

    // 1) moveDate 파싱 (optional)
    // 연·월·일 각각 문자열로 포맷팅 (null이면 그대로 null)
    String yearStr  = (moveYear  != null ? String.valueOf(moveYear) : null);
    String monthStr = (moveMonth != null ? String.format("%02d", moveMonth) : null);
    String dayStr   = (moveDay   != null ? String.format("%02d", moveDay) : null);

    // 2) moveType 유효성 검증 후 문자열로 사용 (optional)
    String moveTypeValue = null;
    if (StringUtils.hasText(moveType)) {
      try {
        MoveType enumType = MoveType.valueOf(moveType.toUpperCase()); // <-- 유효한 enum 값인지 검증
        moveTypeValue = enumType.name(); // <-- 실제 enum 이름 사용
      } catch (IllegalArgumentException ex) {
        log.warn("[EstimatePublicService] moveType 변환 실패 - moveType={}", moveType, ex);
        throw new CustomException(EstimateErrorCode.INVALID_MOVE_TYPE);
      }
    }

    String moveOptionValue = null;
    if (StringUtils.hasText(moveOption)) {
      try {
        MoveOptionType optEnum = MoveOptionType.valueOf(
            moveOption.toUpperCase()); // 유효한 enum 값인지 검증
        moveOptionValue = optEnum.name();                                         // 실제 enum 이름 사용
      } catch (IllegalArgumentException ex) {
        log.warn("[EstimatePublicService] moveOption 변환 실패 - moveOption={}", moveOption, ex);
        throw new CustomException(EstimateErrorCode.INVALID_MOVE_OPTION);
      }
    }

    // 3) 페이징 & 정렬 설정
    Pageable pageable = PageRequest.of(page, size);

    // 4) 실제 조회 (native query)
    Page<Estimate> estimates;

    if (status == EstimateStatus.WAITING) {
      // 기존 공개용 네이티브 쿼리
      estimates = estimateRepository.findPublicEstimatesWithFilters(
          yearStr, monthStr, dayStr,
          moveTypeValue, moveOptionValue,
          fromRegion1, fromRegion2,
          toRegion1,   toRegion2,
          pageable
      );
    } else {
      // 새로 추가할 “ACCEPTED” 전용 네이티브 쿼리
          Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

          Long storeId = store.getId();

      estimates = estimateRepository.findAcceptedEstimatesWithFilters(
          storeId,
          yearStr, monthStr, dayStr,
          moveTypeValue, moveOptionValue,
          fromRegion1, fromRegion2,
          toRegion1,   toRegion2,
          pageable
      );
    }

    // 5) Entity → DTO 변환
    Page<EstimatePreviewResponse> result = estimates.map(EstimatePreviewResponse::fromEntity);
    log.info("result.getContent() size = {}", result.getContent().size());

    log.info("[EstimatePublicService] 공개 과적서 조회 완료 - totalElements={}", result.getTotalElements());

    return result;
  }

  // PENDING(공개) 조회용 래퍼
  public Page<EstimatePreviewResponse> getAvailableEstimates(
      Long userId, int page, int size, Integer moveYear, Integer moveMonth, Integer moveDay,
      String moveType, String moveOption,
      String fromRegion1, String fromRegion2, String toRegion1, String toRegion2
  ) {

    userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    return getEstimatePreview(
        null, page, size, moveYear, moveMonth, moveDay,
        moveType, moveOption, fromRegion1, fromRegion2,
        toRegion1, toRegion2, EstimateStatus.WAITING
    );
  }

  // ACCEPTED(확정) 래퍼: userId → storeId 변환 후 호출
  public Page<EstimatePreviewResponse> getAcceptedEstimates(
      Long userId,
      int page, int size,
      Integer moveYear, Integer moveMonth, Integer moveDay,
      String moveType, String moveOption,
      String fromRegion1, String fromRegion2,
      String toRegion1, String toRegion2
  ) {

    return getEstimatePreview(
        userId,
        page, size,
        moveYear, moveMonth, moveDay,
        moveType, moveOption,
        fromRegion1, fromRegion2,
        toRegion1, toRegion2,
        EstimateStatus.ACCEPTED
        );
  }

  // ✅ [추가] WAITING + 내 ACCEPTED 견적 통합 조회
  public Page<EstimatePreviewResponse> getMergedEstimates(
      Long userId, int page, int size, Integer moveYear, Integer moveMonth, Integer moveDay,
      String moveType, String moveOption,
      String fromRegion1, String fromRegion2, String toRegion1, String toRegion2
  ) {
    // 유효 사용자 확인
    userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // moveDate 파싱
    String yearStr  = (moveYear  != null ? String.valueOf(moveYear) : null);
    String monthStr = (moveMonth != null ? String.format("%02d", moveMonth) : null);
    String dayStr   = (moveDay   != null ? String.format("%02d", moveDay) : null);

    String moveTypeValue = null;
    if (StringUtils.hasText(moveType)) {
      try {
        moveTypeValue = MoveType.valueOf(moveType.toUpperCase()).name();
      } catch (IllegalArgumentException ex) {
        throw new CustomException(EstimateErrorCode.INVALID_MOVE_TYPE);
      }
    }

    String moveOptionValue = null;
    if (StringUtils.hasText(moveOption)) {
      try {
        moveOptionValue = MoveOptionType.valueOf(moveOption.toUpperCase()).name();
      } catch (IllegalArgumentException ex) {
        throw new CustomException(EstimateErrorCode.INVALID_MOVE_OPTION);
      }
    }

    Pageable pageable = PageRequest.of(page, size);

    // WAITING 견적 조회
    Page<Estimate> waiting = estimateRepository.findPublicEstimatesWithFilters(
        yearStr, monthStr, dayStr,
        moveTypeValue, moveOptionValue,
        fromRegion1, fromRegion2,
        toRegion1, toRegion2,
        pageable
    );

    // 내 storeId로 ACCEPTED 견적 조회
    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    Page<Estimate> accepted = estimateRepository.findAcceptedEstimatesWithFilters(
        store.getId(),
        yearStr, monthStr, dayStr,
        moveTypeValue, moveOptionValue,
        fromRegion1, fromRegion2,
        toRegion1, toRegion2,
        pageable
    );

    // 병합 후 변환
//    List<EstimatePreviewResponse> combined = Stream.concat(
//            waiting.getContent().stream(),
//            accepted.getContent().stream()
//        ).map(EstimatePreviewResponse::fromEntity)
//        .collect(Collectors.toList());

    // 3) DTO 변환 리스트 생성 (.toList() 사용)
    List<EstimatePreviewResponse> waitingList = waiting.getContent().stream()
        .map(EstimatePreviewResponse::fromEntity)
        .toList();

    List<EstimatePreviewResponse> acceptedList = accepted.getContent().stream()
        .map(EstimatePreviewResponse::fromEntity)
        .toList();
;

    List<EstimatePreviewResponse> combined = Stream.concat(waitingList.stream(), acceptedList.stream())
        .collect(Collectors.collectingAndThen(
            Collectors.toMap(
                EstimatePreviewResponse::getEstimateNo,
                Function.identity(),
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ),
            map -> new ArrayList<>(map.values())
        ));
    log.info("🔄 병합 후 중복 제거된 estimateNo 목록: {}", combined.stream()
        .map(EstimatePreviewResponse::getEstimateNo)
        .collect(Collectors.toList()));


    // PageImpl로 래핑
    return new PageImpl<>(
        combined,
        pageable,
        waiting.getTotalElements() + accepted.getTotalElements()
    );
  }

}

package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.owner.dto.response.EstimatePreviewResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimatePreviewService {

  private final EstimateRepository estimateRepository;

  // 견적서 페이징
  public Page<EstimatePreviewResponse> getAvailableEstimates(
      Long ownerId,
      int page, // 0부터 시작
      int size,
      Integer moveYear,
      Integer moveMonth,
      Integer moveDay,
      String moveType,  // SMALL, HOME
      String moveOption,
      String fromRegion1, // 서울
      String fromRegion2, // 강남구
      String toRegion1,
      String toRegion2
  ) {
    log.info("[EstimatePublicService] 공개 견적서 조회 시작 - ownerId={}, page={}, size={}, moveYear={}, moveMonth={}, moveDay={}, moveType={}, fromRegion1={}, fromRegion2={}, toRegion1={}, toRegion2={}"
        , ownerId, page, size, moveYear, moveMonth, moveDay, moveType, fromRegion1, fromRegion2, toRegion1, toRegion2);

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
    try {
      estimates = estimateRepository.findPublicEstimatesWithFilters(
          yearStr,
          monthStr,
          dayStr,
          moveTypeValue,
          moveOptionValue,
          fromRegion1,
          fromRegion2,
          toRegion1,
          toRegion2,
          pageable
      );
    } catch (Exception ex) {
      log.error("[EstimatePublicService] 공개 견적서 조회 중 오류 발생", ex);
      throw new CustomException(EstimateErrorCode.FAILED_TO_FETCH_ESTIMATES);
    }

    // 5) Entity → DTO 변환
    Page<EstimatePreviewResponse> result = estimates.map(EstimatePreviewResponse::fromEntity);
    log.info("[EstimatePublicService] 공개 과적서 조회 완료 - totalElements={}", result.getTotalElements());

    return result;
  }
}

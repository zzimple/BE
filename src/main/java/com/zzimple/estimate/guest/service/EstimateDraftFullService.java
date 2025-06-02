package com.zzimple.estimate.guest.service;

import com.zzimple.estimate.guest.dto.response.EstimateDraftFullResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.mapper.EstimateMapper;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.jwt.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstimateDraftFullService {

  private final AddressService addressService;
  private final HolidayService holidayService;
  private final MoveTypeService moveService;
  private final MoveItemsService moveItemsService;
  private final MoveOptionService smallMoveOptionService;
  private final EstimateRepository estimateRepository;
  private final MoveItemsRepository moveItemsRepository;

  // Redis에 임시 저장된 견적서를 DB에 영구 저장합니다.
  @Transactional
  public Long finalizeEstimateDraft(UUID draftId, CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();

    // 1. Redis에서 초안 데이터 전체 조회 (기존 메서드 재활용)
    EstimateDraftFullResponse draft = this.getFullDraft(draftId);

    // 2. Entity로 변환
    Estimate estimate = EstimateMapper.toEntity(draft, userId);

    // 3. 초기 상태 설정
    estimate.setStatus(EstimateStatus.WAITING); // 기본 상태: 대기중

    // 4. DB 저장
    estimateRepository.save(estimate);

    List<MoveItems> moveItems = moveItemsService.toEntityList(
        estimate.getEstimateNo(),
        draft.getMoveItems()
    );

    moveItemsRepository.saveAll(moveItems);

    return estimate.getEstimateNo();
  }


  public EstimateDraftFullResponse getFullDraft(UUID draftId) {
    return EstimateDraftFullResponse.builder()
        .address(addressService.getAddressDraft(draftId))
        .holiday(holidayService.getMoveDate(draftId))
        .moveType(moveService.getMoveType(draftId))
        .moveItems(moveItemsService.getMoveItemsAsResponse(draftId))
        .moveOption(smallMoveOptionService.getOptionIfExist(draftId))
        .build();
  }
}

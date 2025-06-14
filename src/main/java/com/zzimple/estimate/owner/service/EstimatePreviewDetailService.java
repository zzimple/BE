package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.response.EstimateMoveItemsListResponse;
import com.zzimple.estimate.owner.dto.response.EstimatePreviewDetailResponse;
import com.zzimple.estimate.owner.dto.response.MoveItemPreviewDetailResponse;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstimatePreviewDetailService {

  private final EstimateRepository estimateRepository;
  private final MoveItemsRepository moveItemsRepository;

  // 견적서 상세 조회
  public EstimatePreviewDetailResponse getPreviewDraftDetail(Long estimateNo) {
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    List<MoveItems> moveItems = moveItemsRepository.findByEstimateNo(estimateNo);

    return EstimatePreviewDetailResponse.fromEntity(estimate, moveItems);
  }

  // 견적서 물품 조회
  public EstimateMoveItemsListResponse getEstimateMoveItemsList(Long estimateNo) {
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    List<MoveItems> moveItems = moveItemsRepository.findByEstimateNo(estimateNo);

    List<MoveItemPreviewDetailResponse> itemResponses = moveItems.stream()
        .map(MoveItemPreviewDetailResponse::from)
        .toList();

    return EstimateMoveItemsListResponse.builder()
        .items(itemResponses)
        .build();
  }
}

package com.zzimple.estimate.guest.controller;

import com.zzimple.estimate.guest.dto.response.EstimateListDetailResponse;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.service.GuestEstimateService;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/view")
public class EstimateController {

  private final GuestEstimateService guestEstimateService;
  private final OwnerRepository ownerRepository;
  private final StoreRepository storeRepository;
  private final EstimateRepository estimateRepository;

//  @GetMapping("/estimate/{estimateNo}")
//  @Operation(
//      summary = "[고객 | 토큰 O 사장님에게 온 견적서 상세 조회 + 사장 | 토큰 O 견적서 상세 조회]",
//      description = "견적서의 상세 정보를 조회합니다."
//  )
//  @PreAuthorize("hasAnyRole('CUSTOMER','OWNER')")
//  public ResponseEntity<BaseResponse<EstimateListDetailResponse>> getEstimateDetail(
//      @PathVariable Long estimateNo
//  ) {
//    EstimateListDetailResponse response = guestEstimateService.getEstimateDetail(estimateNo);
//    return ResponseEntity.ok(BaseResponse.success(response));
//  }

    @GetMapping("/stores/{storeId}/estimates/{estimateNo}")
    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER')")
    public ResponseEntity<BaseResponse<EstimateListDetailResponse>> getDetail(
        @PathVariable Long storeId,
        @PathVariable Long estimateNo,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
      boolean isOwner = user.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

      if (isOwner) {
        // 사장님 권한: 이 매장이 내 매장인지 확인
        Store store = storeRepository.findByOwnerUserId(user.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("Store not found"));

        if (store.getId() == null || !store.getId().equals(storeId)) {
          throw new AccessDeniedException("매장 접근 권한이 없습니다.");
        }



      } else {
        // 고객 권한: 이 견적이 내 견적인지 확인
        Estimate est = estimateRepository.findByEstimateNo(estimateNo)
            .orElseThrow(() -> new EntityNotFoundException("Estimate not found"));
        if (!est.getUserId().equals(user.getUserId())) {
          throw new AccessDeniedException("이 견적서에 접근 권한이 없습니다.");
        }
      }

      EstimateListDetailResponse dto =
          guestEstimateService.getEstimateDetail(estimateNo, storeId);
      return ResponseEntity.ok(BaseResponse.success(dto));
    }
  }

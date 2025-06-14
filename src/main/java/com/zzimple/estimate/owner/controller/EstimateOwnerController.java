package com.zzimple.estimate.owner.controller;

import com.zzimple.estimate.guest.dto.response.PagedResponse;
import com.zzimple.estimate.owner.dto.request.EstimatePreviewRequest;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.request.SubmitFinalEstimateRequest;
import com.zzimple.estimate.owner.dto.response.CalculateOwnerInputResponse;
import com.zzimple.estimate.owner.dto.response.EstimateMoveItemsListResponse;
import com.zzimple.estimate.owner.dto.response.EstimatePreviewDetailResponse;
import com.zzimple.estimate.owner.dto.response.EstimatePreviewResponse;
import com.zzimple.estimate.owner.dto.response.ItemTotalResultResponse;
import com.zzimple.estimate.owner.dto.response.SaveItemBasePriceResponse;
import com.zzimple.estimate.owner.service.EstimateDetailUpdateService;
import com.zzimple.estimate.owner.service.EstimatePreviewDetailService;
import com.zzimple.estimate.owner.service.EstimatePreviewService;
import com.zzimple.estimate.owner.service.SaveItemExtraPriceService;
import com.zzimple.estimate.owner.service.SaveItemBasePriceService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/estimates/owner")
@RequiredArgsConstructor
public class EstimateOwnerController {

  private final EstimatePreviewService estimatePreviewService;
  private final EstimatePreviewDetailService estimatePreviewDetailService;
  private final SaveItemBasePriceService saveItemBasePriceService;
  private final SaveItemExtraPriceService saveItemExtraPriceService;
  private final EstimateDetailUpdateService estimateDetailUpdateService;

  @Operation(
      summary = "[사장님 | 토큰 O | 공개 견적서 조회]",
      description = "고객이 제출한 공개 견적서 목록을 페이징 및 필터링하여 조회합니다."
  )
  @GetMapping("/list/public")
  public ResponseEntity<BaseResponse<PagedResponse<EstimatePreviewResponse>>> getPublicEstimates(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int page,
      @RequestParam int size,
      @ModelAttribute EstimatePreviewRequest request
  ) {
    Page<EstimatePreviewResponse> result = estimatePreviewService.getAvailableEstimates(
        userDetails.getUserId(),
        page,
        size,
        request.getMoveYear(),
        request.getMoveMonth(),
        request.getMoveDay(),
        request.getMoveType(),
        request.getMoveOption(),
        request.getFromRegion1(),
        request.getFromRegion2(),
        request.getToRegion1(),
        request.getToRegion2()
    );

    PagedResponse<EstimatePreviewResponse> response = PagedResponse.of(result);

    log.info("PagedResponse content size = {}", response.getContent().size());

    return ResponseEntity.ok(BaseResponse.success("공개 견적서 조회 완료", response));
  }


      @Operation(
      summary = "[사장님 | 토큰 O | 견적서 상세조회]",
      description = "고객이 제출한 공개 견적서 조회합니다."
  )
  @GetMapping("/drafts/{estimateNo}")
  public ResponseEntity<BaseResponse<EstimatePreviewDetailResponse>> getDraftDetail(
      @PathVariable Long estimateNo) {
    EstimatePreviewDetailResponse detail = estimatePreviewDetailService.getPreviewDraftDetail(
        estimateNo);
    return ResponseEntity.ok(
        BaseResponse.success("공개 견적서 상세 조회 완료", detail)
    );
  }

  @GetMapping("/{estimateNo}/items")
  public ResponseEntity<BaseResponse<EstimateMoveItemsListResponse>> getEstimateMoveItems(@PathVariable Long estimateNo) {
    EstimateMoveItemsListResponse response = estimatePreviewDetailService.getEstimateMoveItemsList(estimateNo);
    return ResponseEntity.ok(BaseResponse.success("짐 목록 리스트 조회 완료", response));
  }

  @Operation(
      summary = "[사장님 | 토큰 O | 선택 항목 기본 단가 목록 조회]",
      description = "고객이 선택한 짐 항목(itemTypeId)에 대해 사장님이 등록한 기본 단가 정보를 조회합니다."
  )
  @GetMapping("/default-prices")
  public ResponseEntity<BaseResponse<List<SaveItemBasePriceResponse>>> getBasePrices(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam List<Long> itemTypeIds
  ) {
    Long storeId = userDetails.getStoreId();

    List<SaveItemBasePriceResponse> response = saveItemBasePriceService.getBasePrices(storeId,
        itemTypeIds);
    return ResponseEntity.ok(BaseResponse.success("사장님 단가 불러오기 성공", response));
  }

  @Operation(
      summary = "[사장님 | 토큰 O | 견적서 물품 최종 가격 저장]",
      description = "사장님이 견적서 항목에 기본 금액과 추가금 포함해서 저장합니다."
  )
  @PostMapping("/drafts/{estimateNo}/items")
  public ResponseEntity<BaseResponse<String>> saveEstimateItems(
      @PathVariable Long estimateNo,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody List<SaveEstimatePriceRequest> requests) {

    Long storeId = userDetails.getStoreId();

    saveItemExtraPriceService.saveEstimateItems(estimateNo, storeId, requests);
    return ResponseEntity.ok(BaseResponse.success("견적서 항목 저장 완료"));
  }

  @Operation(
      summary = "[사장님 | 토큰 O | 견적서 물품 기본금 + 추가금 계산기]",
      description = "저장 없이 견적 항목들의 기본금 + 추가금을 계산하여 프론트에 반환합니다.")
  @PostMapping("/drafts/{estimateNo}/items/item-total")
  public ResponseEntity<BaseResponse<ItemTotalResultResponse>> ItemTotalPrices(
      @PathVariable Long estimateNo,
      @AuthenticationPrincipal CustomUserDetails userDetails
      ) {

    Long storeId = userDetails.getStoreId();

    ItemTotalResultResponse result = saveItemExtraPriceService.calculateAndSaveItemTotalPrices(estimateNo, storeId);
    return ResponseEntity.ok(BaseResponse.success("총 금액 계산 완료", result));
  }


  @Operation(summary = "[사장님 | 견적 추가사항 최종 입력 저장]")
  @PostMapping("/drafts/{estimateNo}/save-owner-input")
  public ResponseEntity<BaseResponse<String>> saveOwnerInput(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long estimateNo,
      @RequestBody SubmitFinalEstimateRequest request
  ) {
    Long storeId = userDetails.getStoreId();

    estimateDetailUpdateService.saveOwnerInput(storeId, estimateNo, request);
    return ResponseEntity.ok(BaseResponse.success("사장님 입력 정보가 저장되었습니다."));
  }


  @Operation(
      summary = "[사장님 | 토큰 O | 최종 합계 계산 및 저장]",
      description = "items_total_price 및 estimate_extra_charge 합산 → estimate_calculation 에 저장합니다."
  )
  @PostMapping("/drafts/{estimateNo}/calculate-and-save-final")
  public ResponseEntity<BaseResponse<CalculateOwnerInputResponse>> calculateAndSaveFinalTotals(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long estimateNo
  ) {
    Long storeId = userDetails.getStoreId();

    // 1) 계산하고 저장만 수행
    estimateDetailUpdateService.calculateAndSaveFinalTotals(storeId, estimateNo);

    // 1) 계산 + 저장 + DTO 생성(반환)
    CalculateOwnerInputResponse resp =
        estimateDetailUpdateService.calculateAndSaveFinalTotals(storeId, estimateNo);

    // 3) 결과 반환
    return ResponseEntity.ok(
        BaseResponse.success("최종 합계 계산 및 저장 완료", resp)
    );
  }
}

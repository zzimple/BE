package com.zzimple.estimate.guest.controller;

import com.zzimple.estimate.guest.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.guest.dto.request.HolidaySaveRequest;
import com.zzimple.estimate.guest.dto.request.MoveItemsBatchRequest;
import com.zzimple.estimate.guest.dto.request.MoveOptionTypeRequest;
import com.zzimple.estimate.guest.dto.request.MoveTypeRequest;
import com.zzimple.estimate.guest.dto.response.AddressDraftResponse;
import com.zzimple.estimate.guest.dto.response.EstimateDraftFullResponse;
import com.zzimple.estimate.guest.dto.response.MonthlyHolidayPreviewResponse;
import com.zzimple.estimate.guest.dto.response.HolidaySaveResponse;
import com.zzimple.estimate.guest.dto.response.MoveItemsDraftResponse;
import com.zzimple.estimate.guest.dto.response.MoveOptionTypeResponse;
import com.zzimple.estimate.guest.dto.response.MoveTypeResponse;
import com.zzimple.estimate.guest.service.AddressService;
import com.zzimple.estimate.guest.service.EstimateDraftFullService;
import com.zzimple.estimate.guest.service.HolidayService;
import com.zzimple.estimate.guest.service.MoveItemsService;
import com.zzimple.estimate.guest.service.MoveOptionService;
import com.zzimple.estimate.guest.service.MoveTypeService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/estimates/draft")
@RequiredArgsConstructor
public class EstimateDraftController {

  private final AddressService addressService;
  private final HolidayService holidayService;
  private final MoveItemsService moveItemsService;
  private final MoveTypeService moveTypeService;
  private final MoveOptionService moveOptionService;
  private final EstimateDraftFullService estimateDraftFullService;

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - draftId 생성 ]",
      description = "견적서를 작성하기 위해 고유한 draftId(UUID)를 생성합니다. 이후 모든 API 요청 시 draftId를 쿼리로 넘겨야 합니다."
  )
  @PostMapping("/start")
  public ResponseEntity<BaseResponse<Map<String, UUID>>> startEstimateDraft() {
    UUID draftId = UUID.randomUUID();
    Map<String, UUID> result = Map.of("draftId", draftId);
    log.info("[EstimateDraft] draftId 발급: {}", draftId);
    return ResponseEntity.ok(BaseResponse.success("draftId 생성이 완료 되었습니다.",result));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 주소 임시 저장 ]",
      description =
          """
           **Parameters**  \n
           fromAddress: 출발 주소 정보 (JSON 객체) \s
           roadFullAddr: 전체 도로명 주소 (예: 서울특별시 강남구 테헤란로 223, 20층 (역삼동)) \s
           roadAddrPart1: 기본 도로명 주소 \s
           addrDetail: 상세 주소 (예: 20층) \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s
           toAddress: 도착 주소 정보 (JSON 객체) \s
           roadFullAddr: 전체 도로명 주소 \s
           roadAddrPart1: 기본 도로명 주소 \s
           addrDetail: 상세 주소 \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s

           **Returns**  \n
           roadAddrPart1: 도로명 주소 (전체x)  \n
           message: 임시 저장 성공 여부  \n
           """)
  @PostMapping("/address")
  public ResponseEntity<BaseResponse<AddressDraftResponse>> saveAddress(
      @RequestParam UUID draftId,
      @RequestBody AddressDraftSaveRequest request
  ) {
    AddressDraftResponse response = addressService.saveAddressDraft(draftId, request);
    return ResponseEntity.ok(BaseResponse.success("주소가 저장 되었습니다.", response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 월별 공휴일 미리보기 ]",
      description =
          """
          **Parameters**  
          yearMonth: 확인할 월 (형식: yyyyMM)  
  
          **Returns**  
          월의 각 날짜별 공휴일 정보 리스트 (date, holiday, dateName)
          """
  )
  @GetMapping("/holidays/preview")
  public ResponseEntity<BaseResponse<List<MonthlyHolidayPreviewResponse>>> previewMonthlyHolidays(
      @RequestParam String yearMonth) {
    List<MonthlyHolidayPreviewResponse> result = holidayService.previewMonthlyHolidays(yearMonth);
    return ResponseEntity.ok(BaseResponse.success("월별 공휴일 정보 조회 완료", result));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 공휴일 저장 ]",
      description =
          """
          **Parameters**  \n
          draftId: 사용자별 UUID  \n
          date: 저장할 날짜 (형식: yyyyMMdd)  \n
          time: 저장할 시간 (형식: HH:mm)  \n
          
          **Returns**  \n
          movedate: 저장된 이사 날짜 (형식: yyyyMMdd)  \n
          movetime: 저장된 이사 시간 (형식: HH:mm)  \n
          """
  )
  @PostMapping("/holiday/save")
  public ResponseEntity<BaseResponse<HolidaySaveResponse>> checkHoliday(
      @RequestParam UUID draftId,
      @RequestBody HolidaySaveRequest holidaySaveRequest) {
    HolidaySaveResponse result = holidayService.saveMoveDate(draftId, holidaySaveRequest.getDate(), holidaySaveRequest.getTime());
    return ResponseEntity.ok(BaseResponse.success("공휴일 저장 및 조회 완료", result));
  }

  @Operation(
      summary = "[고객 | 토큰 O | 견적서 - 짐 항목 일괄 저장]",
      description = "전체 박스 개수와 모든 짐 항목 리스트를 받아 Redis에 덮어씁니다."
  )
  @PatchMapping(value = "/move-items", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<MoveItemsDraftResponse>> saveAllMoveItems(
      @RequestParam UUID draftId,
      @RequestBody MoveItemsBatchRequest batchRequest
  ) {
    MoveItemsDraftResponse response = moveItemsService.saveAllMoveItems(draftId, batchRequest);
    return ResponseEntity.ok(BaseResponse.success("짐 항목 일괄 저장이 완료되었습니다.", response));
  }

  @Operation(
      summary = "[고객 | 토큰 O | 견적서 - 짐 항목 전체 조회]",
      description = "저장된 모든 짐 항목과 전체 박스 개수를 조회합니다."
  )
  @GetMapping("/move-items")
  public ResponseEntity<BaseResponse<MoveItemsDraftResponse>> getAllMoveItems(
      @RequestParam UUID draftId
  ) {
    MoveItemsDraftResponse response = moveItemsService.getMoveItemsAsResponse(draftId);
    return ResponseEntity.ok(BaseResponse.success("짐 항목 조회가 완료되었습니다.", response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 이사 유형 저장 ]",
      description = """
        **Parameters**  \n
        draftId: 견적서 고유 ID(UUID)  \n

        **Returns**  \n
        moveType: 저장된 이사 유형  \n
        """
  )
  @PostMapping("/move-type")
  public ResponseEntity<BaseResponse<MoveTypeResponse>> saveMoveType(
      @RequestParam UUID draftId,
      @RequestBody MoveTypeRequest request
  ) {
    MoveTypeResponse response = moveTypeService.saveMoveType(draftId, request);
    return ResponseEntity.ok(BaseResponse.success("이사 유형이 저장되었습니다.", response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 소형 이사 옵션 저장 ]",
      description = """
        **Parameters**  \n
        draftId: 견적서 고유 ID(UUID)  \n
        optionType: 선택한 이사 옵션 (BASIC / PACKAGING / SEMI_PACKAGING)  \n

        **Returns**  \n
        optionType: 저장된 이사 옵션  \n
        """
  )
  @PostMapping("/move-option")
  public ResponseEntity<BaseResponse<MoveOptionTypeResponse>> saveMoveOptionType(
      @RequestParam UUID draftId,
      @RequestBody MoveOptionTypeRequest request
  ) {
    MoveOptionTypeResponse response = moveOptionService.saveMoveOptionType(draftId, request);
    return ResponseEntity.ok(BaseResponse.success("소형 이사 옵션이 저장되었습니다.", response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 모든 데이터 통합 조회 ]",
      description = "draftId를 기준으로 주소, 이사유형, 짐 목록, 공휴일, 옵션 등을 통합 조회합니다."
  )
  @GetMapping("/full")
  public ResponseEntity<BaseResponse<EstimateDraftFullResponse>> getFullDraft(@RequestParam UUID draftId) {
    EstimateDraftFullResponse response = estimateDraftFullService.getFullDraft(draftId);
    return ResponseEntity.ok(BaseResponse.success("견적 초안 전체 데이터를 조회했습니다.", response));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 최종 제출 → DB 저장 ]",
      description = """
              draftId로 Redis에 임시 저장된 견적서를 DB에 영구 저장합니다. \n
              저장이 완료되면 estimateNo(PK)가 반환됩니다.
          """
  )
  @PostMapping("/finalize/{draftId}")
  public ResponseEntity<BaseResponse<Long>> finalizeEstimate(
      @PathVariable UUID draftId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long estimateNo = estimateDraftFullService.finalizeEstimateDraft(draftId, userDetails);
    return ResponseEntity.ok(BaseResponse.success("견적서가 저장되었습니다.", estimateNo));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 임시 저장된 초안 전체 조회 (단계별 복원용)]",
      description = "draftId 기준으로 Redis에 임시 저장된 모든 견적서 데이터를 통합 조회합니다."
  )
  @GetMapping("/load/{draftId}")
  public ResponseEntity<BaseResponse<EstimateDraftFullResponse>> getDraftData(
      @PathVariable UUID draftId
  ) {
    EstimateDraftFullResponse response = estimateDraftFullService.getFullDraft(draftId);
    return ResponseEntity.ok(BaseResponse.success("임시 저장된 견적 초안 데이터를 조회했습니다.", response));
  }



}
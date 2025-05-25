package com.zzimple.estimate.controller;

import com.zzimple.estimate.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.dto.request.MoveItemsBatchRequest;
import com.zzimple.estimate.dto.request.MoveOptionTypeRequest;
import com.zzimple.estimate.dto.request.MoveTypeDraftRequest;
import com.zzimple.estimate.dto.response.AddressDraftResponse;
import com.zzimple.estimate.dto.response.EstimateDraftFullResponse;
import com.zzimple.estimate.dto.response.HolidayPreviewResponse;
import com.zzimple.estimate.dto.response.HolidaysaveResponse;
import com.zzimple.estimate.dto.response.MoveItemsDraftResponse;
import com.zzimple.estimate.dto.response.MoveOptionTypeResponse;
import com.zzimple.estimate.dto.response.MoveTypeResponse;
import com.zzimple.estimate.service.AddressService;
//import com.zzimple.estimate.service.EstimateDraftFullService;
import com.zzimple.estimate.service.EstimateDraftFullService;
import com.zzimple.estimate.service.HolidayService;
import com.zzimple.estimate.service.MoveItemsService;
import com.zzimple.estimate.service.MoveOptionService;
import com.zzimple.estimate.service.MoveTypeService;
import com.zzimple.global.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
           roadAddr: 기본 도로명 주소 \s
           addrDetail: 상세 주소 (예: 20층) \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s
           toAddress: 도착 주소 정보 (JSON 객체) \s
           roadFullAddr: 전체 도로명 주소 \s
           roadAddr: 기본 도로명 주소 \s
           addrDetail: 상세 주소 \s
           zipNo: 우편번호 \s
           buldMgtNo: 건물 관리 번호 \s

           **Returns**  \n
           roadAddr: 도로명 주소 (전체x)  \n
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
      summary = "[ 고객 | 토큰 O | 공휴일 미리보기 ]",
      description =
          """
          **Parameters**  \n
          date: 확인할 날짜 (형식: yyyyMMdd)  \n
  
          **Returns**  \n
          isHoliday: 공휴일 여부 (Y / N)  \n
          dateName: 공휴일 이름 (예: 어린이날), 공휴일이 아닐 경우 null  \n
          """
  )
  @GetMapping("/holiday/preview")
  public ResponseEntity<BaseResponse<HolidayPreviewResponse>> previewHoliday(
      @RequestParam String date) {
    HolidayPreviewResponse result = holidayService.previewHoliday(date);
    return ResponseEntity.ok(BaseResponse.success("공휴일 여부 미리보기 조회 완료", result));
  }

  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 공휴일 저장 ]",
      description =
          """
          **Parameters**  \n
          draftId: 사용자별 UUID  \n
          date: 확인할 날짜 (형식: yyyyMMdd)  \n
  
          **Returns**  \n
          movedate: 저장된 이사 날짜 (형식: yyyyMMdd)  \n
          """
  )
  @GetMapping("/holiday/save")
  public ResponseEntity<BaseResponse<HolidaysaveResponse>> checkHoliday(
      @RequestParam UUID draftId,
      @RequestParam String date) {
    HolidaysaveResponse result = holidayService.saveMoveDate(draftId, date);
    return ResponseEntity.ok(BaseResponse.success("공휴일 저장 및 조회 완료", result));
  }

  @Operation(
      summary = "[고객 | 토큰 O | 견적서 - 짐 항목 일괄 저장]",
      description = "전체 박스 개수와 모든 짐 항목 리스트를 받아 Redis에 덮어씁니다."
  )
  @PutMapping(value = "/move-items", consumes = MediaType.APPLICATION_JSON_VALUE)
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
      @RequestBody MoveTypeDraftRequest request
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
}

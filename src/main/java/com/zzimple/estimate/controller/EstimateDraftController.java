package com.zzimple.estimate.controller;

import com.zzimple.estimate.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.dto.request.MoveItemsDraftRequest;
import com.zzimple.estimate.dto.response.AddressDraftResponse;
import com.zzimple.estimate.dto.response.HolidayCheckResponse;
import com.zzimple.estimate.dto.response.MoveItemsDraftResponse;
import com.zzimple.estimate.service.AddressService;
import com.zzimple.estimate.service.HolidayService;
import com.zzimple.estimate.service.MoveItemsService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
      summary = "[ 고객 | 토큰 O | 견적서 - 공휴일 여부 확인 및 저장 ]",
      description =
          """
          **Parameters**  \n
          date: 확인할 날짜 (형식: yyyyMMdd)  \n
  
          **Returns**  \n
          isHoliday: 공휴일 여부 (Y / N)  \n
          dateName: 공휴일 이름 (예: 어린이날), 공휴일이 아닐 경우 null  \n
          """
  )
  @GetMapping("/holiday/check")
  public ResponseEntity<BaseResponse<HolidayCheckResponse>> checkHoliday(
      @RequestParam UUID draftId,
      @RequestParam String date
  ) {
    HolidayCheckResponse result = holidayService.checkHoliday(draftId, date);
    return ResponseEntity.ok(BaseResponse.success("공휴일 저장 완료되었습니다.",result));
  }


  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 짐 항목 하나 추가 ]",
      description = "사용자가 선택한 짐 항목 하나를 Redis에 추가 저장합니다."
  )
  @PostMapping("/move-item")
  public ResponseEntity<BaseResponse<MoveItemsDraftResponse>> addMoveItem(
      @RequestParam UUID draftId,
      @RequestBody MoveItemsDraftRequest itemDto
  ) {
    MoveItemsDraftResponse response = moveItemsService.appendMoveItem(draftId, itemDto);
    return ResponseEntity.ok(BaseResponse.success("짐 추가가 완료되었습니다.",response));
  }


  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 짐 항목 하나 삭제 ]",
      description = "이미 선택한 짐 항목(itemTypeId 기준)을 삭제합니다."
  )
  @DeleteMapping("/move-item/{entryId}")
  public ResponseEntity<BaseResponse<Void>> removeMoveItem(
      @RequestParam UUID draftId,
      @PathVariable String entryId
  ) {
    moveItemsService.removeMoveItemByEntryId(draftId, entryId);
    return ResponseEntity.ok(BaseResponse.success("짐 삭제가 완료되었습니다.",null));
  }


  @Operation(
      summary = "[ 고객 | 토큰 O | 견적서 - 저장된 짐 항목 전체 조회 ]",
      description = "Redis에 임시 저장된 MoveItemRequestDto 리스트를 조회합니다."
  )
  @GetMapping("/move-item")
  public ResponseEntity<BaseResponse<MoveItemsDraftResponse>> getMoveItems(
      @RequestParam UUID draftId
  ) {
    MoveItemsDraftResponse response = moveItemsService.getMoveItemsAsResponse(draftId);
    return ResponseEntity.ok(BaseResponse.success("모든 짐 리스트를 조회했습니다.",response));
  }
}

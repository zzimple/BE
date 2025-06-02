package com.zzimple.estimate.guest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.guest.dto.request.MoveItemsBatchRequest;
import com.zzimple.estimate.guest.dto.request.MoveItemsDraftRequest;
import com.zzimple.estimate.guest.dto.response.MoveItemsDraftResponse;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.estimate.guest.exception.MoveItemErrorCode;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoveItemsService {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private static final Duration TTL = Duration.ofHours(1);

  // 기본 키
  private String keyOf(UUID draftId) {
    return RedisKeyUtil.draftMoveItemsKey(draftId);
  }

  // 짐 목록 저장용, 박스 개수용 키
  private String itemsKey(UUID draftId) {
    return keyOf(draftId) + ":items";
  }
  private String boxKey(UUID draftId) {
    return keyOf(draftId) + ":box";
  }
  private String leftoverBoxKey(UUID draftId) {
    return keyOf(draftId) + ":leftoverBox";
  }
  private String requestNoteKey(UUID draftId) {
    return keyOf(draftId) + ":requestNote";
  }

  // 전체 박스 개수랑, 짐 목록들 가져와서 redis에 저장
  public MoveItemsDraftResponse saveAllMoveItems(UUID draftId, MoveItemsBatchRequest batch) {
    int boxCount = batch.getBoxCount();
    int leftoverBoxCount  = batch.getLeftoverBoxCount();
    List<MoveItemsDraftRequest> items = batch.getItems();
    String requestNote = batch.getRequestNote();

    log.info("[saveAllMoveItems] draftId={} save {} items with boxCount={} leftoverBoxCount={}, note={}",
        draftId, items.size(), boxCount, leftoverBoxCount, requestNote);

    // 1) 기존 데이터 삭제
    redisTemplate.delete(itemsKey(draftId));
    redisTemplate.delete(boxKey(draftId));
    redisTemplate.delete(leftoverBoxKey(draftId));
    redisTemplate.delete(requestNoteKey(draftId));

    // 2) 박스 개수 저장
    redisTemplate.opsForValue()
        .set(boxKey(draftId), String.valueOf(boxCount), TTL);
    redisTemplate.opsForValue()
        .set(leftoverBoxKey(draftId), String.valueOf(leftoverBoxCount), TTL);

    // 2-1) 요청 사항 저장
    redisTemplate.opsForValue()
        .set(requestNoteKey(draftId), requestNote, TTL);

    // 3) 아이템 리스트 저장
    ListOperations<String, String> ops = redisTemplate.opsForList();
    List<MoveItemsDraftResponse.MoveItemResponseDto> dtos = new ArrayList<>();
    for (MoveItemsDraftRequest req : items) {
      try {
        String json = objectMapper.writeValueAsString(req);
        ops.rightPush(itemsKey(draftId), json);
        dtos.add(toDto(req));
      } catch (JsonProcessingException ex) {
        log.error("serialization failed for entryId={}", req.getEntryId(), ex);
        throw new CustomException(MoveItemErrorCode.JSON_SERIALIZE_FAIL);
      }
    }
    redisTemplate.expire(itemsKey(draftId), TTL);

    return new MoveItemsDraftResponse(
        boxCount,
        leftoverBoxCount,
        requestNote,
        dtos
    );
  }

  // 저장된 짐 항목과 박스 개수를 조회
  public MoveItemsDraftResponse getMoveItemsAsResponse(UUID draftId) {
    // 1) 아이템 JSON 리스트 불러오기
    List<String> jsons = redisTemplate.opsForList()
        .range(itemsKey(draftId), 0, -1);

    // 2) JSON → DTO 파싱
    List<MoveItemsDraftRequest> saved = Optional.ofNullable(jsons)
        .orElseGet(Collections::emptyList)
        .stream()
        .map(j -> {
          try {
            return objectMapper.readValue(j, MoveItemsDraftRequest.class);
          } catch (JsonProcessingException e) {
            throw new CustomException(MoveItemErrorCode.JSON_PARSE_FAIL);
          }
        })
        .toList();   // ← collect(Collectors.toList()) 대신 toList()

    // 3) 박스 개수 조회
    int boxCount = Optional.ofNullable(redisTemplate.opsForValue().get(boxKey(draftId)))
        .map(Integer::parseInt)
        .orElse(0);
    int leftoverBoxCount = Optional.ofNullable(redisTemplate.opsForValue().get(leftoverBoxKey(draftId)))
        .map(Integer::parseInt)
        .orElse(0);

    String requestNote = redisTemplate.opsForValue()
        .get(requestNoteKey(draftId));


    // 4) 응답 DTO 변환
    List<MoveItemsDraftResponse.MoveItemResponseDto> dtos = saved.stream()
        .map(this::toDto)
        .toList();

    return new MoveItemsDraftResponse(
        boxCount,
        leftoverBoxCount,
        requestNote,
        dtos
    );
  }

  // DTO 변환 헬퍼
  private MoveItemsDraftResponse.MoveItemResponseDto toDto(MoveItemsDraftRequest request) {
    return new MoveItemsDraftResponse.MoveItemResponseDto(
        request.getEntryId(),
        request.getItemTypeId() != null ? request.getItemTypeId().longValue() : null,
        request.getItemTypeName(),
        request.getCategory(),
        request.getQuantity(),
        request.getType(),
        request.getWidth(),
        request.getHeight(),
        request.getDepth(),
        request.getMaterial(),
        request.getSize(),
        request.getShape(),
        request.getCapacity(),
        request.getDoorCount(),
        request.getUnitCount(),
        request.getFrame(),
        Boolean.TRUE.equals(request.getHasGlass()),
        Boolean.TRUE.equals(request.getIsFoldable()),
        Boolean.TRUE.equals(request.getHasWheels()),
        Boolean.TRUE.equals(request.getHasPrinter()),
        request.getPurifierType(),
        request.getSpecialNote()
    );
  }

  public MoveItems toEntity(MoveItemsDraftResponse.MoveItemResponseDto response, Long estimateNo) {
    return MoveItems.builder()
        .estimateNo(estimateNo)
        .itemTypeName(response.getItemTypeName())
        .category(response.getCategory())
        .itemTypeId(response.getItemTypeId() != null ? response.getItemTypeId().longValue() : null)
        .quantity(response.getQuantity())
        .type(response.getType())
        .width(response.getWidth())
        .height(response.getHeight())
        .depth(response.getDepth())
        .material(response.getMaterial())
        .size(response.getSize())
        .shape(response.getShape())
        .capacity(response.getCapacity())
        .doorCount(response.getDoorCount())
        .unitCount(response.getUnitCount())
        .frame(response.getFrame())
        .hasGlass(response.isGlass())
        .foldable(response.isFoldable())
        .hasWheels(response.isWheels())
        .hasPrinter(response.isPrinter())
        .purifierType(response.getPurifierType())
        .specialNote(response.getSpecialNote())
        .build();
  }

  public List<MoveItems> toEntityList(Long estimateNo, MoveItemsDraftResponse response) {
    return response.getItems().stream()
        .map(dto -> toEntity(dto, estimateNo))
        .collect(Collectors.toList());
  }
}

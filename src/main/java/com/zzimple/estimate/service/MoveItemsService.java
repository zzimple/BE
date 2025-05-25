package com.zzimple.estimate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.request.MoveItemsBatchRequest;
import com.zzimple.estimate.dto.request.MoveItemsDraftRequest;
import com.zzimple.estimate.dto.response.MoveItemsDraftResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.MoveItemErrorCode;
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
  private static final Duration EXPIRE = Duration.ofHours(1);

  // 기본 키
  private String keyOf(UUID draftId) {
    return RedisKeyUtil.draftMoveItemsKey(draftId);
  }

  // 짐 목록 저장용, 박스 개수용 키
  private String itemsKey(UUID draftId) { return keyOf(draftId) + ":items"; }
  private String boxKey(UUID draftId)   { return keyOf(draftId) + ":box"; }


  // 전체 박스 개수랑, 짐 목록들 가져와서 redis에 저장
  public MoveItemsDraftResponse saveAllMoveItems(UUID draftId, MoveItemsBatchRequest batch) {
    int boxCount = batch.getBoxCount();
    List<MoveItemsDraftRequest> items = batch.getItems();
    log.info("[saveAllMoveItems] draftId={} save {} items with boxCount={}",
        draftId, items.size(), boxCount);

    // 1) 기존 데이터 삭제
    redisTemplate.delete(itemsKey(draftId));
    redisTemplate.delete(boxKey(draftId));

    // 2) 박스 개수 저장
    redisTemplate.opsForValue()
        .set(boxKey(draftId), String.valueOf(boxCount), EXPIRE);

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
    redisTemplate.expire(itemsKey(draftId), EXPIRE);

    return new MoveItemsDraftResponse(boxCount, dtos);
  }

  /**
   * 저장된 짐 항목과 박스 개수를 조회
   */
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
    String boxVal = redisTemplate.opsForValue().get(boxKey(draftId));
    int boxCount = boxVal != null ? Integer.parseInt(boxVal) : 0;

    // 4) 응답 DTO 변환
    List<MoveItemsDraftResponse.MoveItemResponseDto> dtos = saved.stream()
        .map(this::toDto)
        .toList();  // ← collect(Collectors.toList()) 대신 toList()

    return new MoveItemsDraftResponse(boxCount, dtos);
  }


  // DTO 변환 헬퍼
  private MoveItemsDraftResponse.MoveItemResponseDto toDto(MoveItemsDraftRequest req) {
    return new MoveItemsDraftResponse.MoveItemResponseDto(
        req.getEntryId(),
        req.getItemTypeId() != null ? req.getItemTypeId().longValue() : null,
        req.getQuantity(),
        req.getWidth(),
        req.getHeight(),
        req.getDepth(),
        req.getMaterial(),
        req.getSize(),
        req.getShape(),
        req.getCapacity(),
        req.getDoorCount(),
        req.getUnitCount(),
        req.getFrame(),
        Boolean.TRUE.equals(req.getHasGlass()),
        Boolean.TRUE.equals(req.getIsFoldable()),
        Boolean.TRUE.equals(req.getHasWheels()),
        Boolean.TRUE.equals(req.getHasPrinter()),
        req.getPurifierType(),
        req.getAcType(),
        req.getSpecialNote(),
        req.getRequestNote(),
        req.getDetails()
    );
  }
}

package com.zzimple.estimate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.request.MoveItemsDraftRequest;
import com.zzimple.estimate.dto.response.MoveItemsDraftResponse;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.MoveItemErrorCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoveItemsService {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  private static final Duration EXPIRE = Duration.ofHours(1);

  // Redis Key
  private String keyOf(UUID draftId) {
    return String.format("estimate:draft:%s:move-items", draftId);
  }

  // 1) 저장된 요청 불러오기
  private List<MoveItemsDraftRequest> loadSaved(UUID draftId) throws JsonProcessingException {
    log.info("[loadSaved] draftId={} 호출", draftId);

    // Redis 리스트에서 해당 draftId 키에 저장된 모든 항목(JSON 문자열)을 조회함
    List<String> jsons = redisTemplate.opsForList().range(keyOf(draftId), 0, -1);

    if (jsons == null) {
      log.info("[loadSaved] draftId={} 저장된 JSON 없음", draftId);
      return List.of();
    }

    List<MoveItemsDraftRequest> list = new ArrayList<>();

    // JSON 문자열들을 MoveItemsDraftRequest 객체로 역직렬화해서 리스트에 추가하고, 각 항목의 entryId를 로그로 출력함
    for (String j : jsons) {
      try {
        MoveItemsDraftRequest r = objectMapper.readValue(j, MoveItemsDraftRequest.class);
        list.add(r);
        log.debug("[loadSaved] draftId={} → 불러온 엔트리 entryId={}", draftId, r.getEntryId());
      } catch (JsonProcessingException e) {
        log.error("[loadSaved] draftId={} JSON 역직렬화 실패", draftId, e);
        throw new CustomException(MoveItemErrorCode.JSON_PARSE_FAIL);
      }
    }
    log.info("[loadSaved] draftId={} 총 {}개 엔트리 반환", draftId, list.size());
    return list;
  }

  // 2) Request → Response DTO 변환
  private MoveItemsDraftResponse.MoveItemResponseDto toDto(MoveItemsDraftRequest request) {
    log.debug("[toDto] entryId={} 변환 시작", request.getEntryId());

    // 역직렬화된 항목 객체에서 entryId 값을 추출함
    String eid = request.getEntryId();

    // Boolean 필드가 null이어도 NPE 없이 안전하게 true인지 확인함
    boolean safeGlass   = Boolean.TRUE.equals(request.getHasGlass());
    boolean safeFold    = Boolean.TRUE.equals(request.getIsFoldable());
    boolean safeWheels  = Boolean.TRUE.equals(request.getHasWheels());
    boolean safePrinter = Boolean.TRUE.equals(request.getHasPrinter());

    // details가 null이면 빈 맵을 사용하고, 아니면 복사하여 새 맵으로 생성함
    Map<String,Object> det = request.getDetails()!=null
        ? new HashMap<>(request.getDetails())
        : Collections.emptyMap();

    log.debug("[toDto] entryId={} 변환 완료", request.getEntryId());

    return new MoveItemsDraftResponse.MoveItemResponseDto(
        eid,
        request.getItemTypeId()!=null? request.getItemTypeId().longValue(): null,
        request.getQuantity(),
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
        safeGlass,
        safeFold,
        safeWheels,
        safePrinter,
        request.getPurifierType(),
        request.getAcType(),
        request.getSpecialNote(),
        request.getRequestNote(),
        det,
        request.getBox()
    );
  }

  // 3) 추가 메서드: 엔트리별 추가
  public MoveItemsDraftResponse appendMoveItem(UUID draftId, MoveItemsDraftRequest request) {
    log.info("[appendMoveItem] draftId={} itemTypeId={} 진입", draftId, request.getItemTypeId());

    try {
      // 요청 객체를 JSON 문자열로 직렬화하여 Redis 리스트에 저장함
      // -> Redis는 자바 객체를 직접 저장할 수 없기 때문

      String json = objectMapper.writeValueAsString(request);
      redisTemplate.opsForList().rightPush(keyOf(draftId), json);
      redisTemplate.expire(keyOf(draftId), EXPIRE);

      log.info("[appendMoveItem] draftId={} entryId={} 저장됨", draftId, request.getEntryId());

      // 요청 객체를 바로 DTO로 변환하여 단일 항목만 반환
      MoveItemsDraftResponse.MoveItemResponseDto dto = toDto(request);

      return new MoveItemsDraftResponse(List.of(dto));
    } catch (JsonProcessingException ex) {
      log.error("[appendMoveItem] draftId={} JSON 처리 오류", draftId, ex);
      throw new CustomException(MoveItemErrorCode.JSON_SERIALIZE_FAIL);
    }
  }


  // 4) 삭제: entryId 기준
  public void removeMoveItemByEntryId(UUID draftId, String entryId) {
    log.info("[removeMoveItem] draftId={} entryId={} 진입", draftId, entryId);

    // Redis 리스트 연산 객체를 가져옴
    ListOperations<String,String> ops = redisTemplate.opsForList();

    List<MoveItemsDraftRequest> saved;

    // Redis에 저장된 항목들을 불러오고, JSON 파싱 중 오류가 나면 예외 처리함
    try {
      saved = loadSaved(draftId);
    } catch (JsonProcessingException e) {
      log.error("[removeMoveItem] draftId={} JSON 파싱 오류", draftId, e);
      throw new CustomException(MoveItemErrorCode.JSON_SERIALIZE_FAIL);
    }


    for (MoveItemsDraftRequest request : saved) {

      // 만약에 같으면
      if (entryId.equals(request.getEntryId())) {

        try {
          String j = objectMapper.writeValueAsString(request);
          ops.remove(keyOf(draftId), 0, j);
          log.info("[removeMoveItem] draftId={} entryId={} 삭제됨", draftId, entryId);
        } catch (JsonProcessingException ignored) {
          log.warn("[removeMoveItem] draftId={} entryId={} 삭제할 JSON 변환 실패", draftId, entryId);
        }
        break;
      }
    }
    redisTemplate.expire(keyOf(draftId), EXPIRE);
  }

  // 5) 전체 조회 as Response
  public MoveItemsDraftResponse getMoveItemsAsResponse(UUID draftId) {
    log.info("[getMoveItems] draftId={} 조회 요청", draftId);

    List<MoveItemsDraftRequest> saved;

    try {
      saved = loadSaved(draftId);
    } catch (JsonProcessingException e) {
      log.error("[getMoveItems] draftId={} JSON 파싱 오류", draftId, e);
      throw new RuntimeException(e);
    }

    // 전체 조회
    List<MoveItemsDraftResponse.MoveItemResponseDto> dto = saved.stream().map(this::toDto).toList();

    log.info("[getMoveItems] draftId={} 반환할 항목 수={}", draftId, dto.size());

    return new MoveItemsDraftResponse(dto);
  }
}

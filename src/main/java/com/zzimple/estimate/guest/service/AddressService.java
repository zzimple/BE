package com.zzimple.estimate.guest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.guest.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.guest.dto.request.AddressWithDetailRequest;
import com.zzimple.estimate.guest.dto.response.AddressDraftResponse;
import com.zzimple.estimate.guest.dto.response.AddressFullResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.estimate.guest.exception.AddressErrorCode;
import com.zzimple.global.exception.CustomException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

  private final StringRedisTemplate redisTemplate;

  // JSON 파싱을 위한 ObjectMapper
  private final ObjectMapper objectMapper;

  private static final Duration TTL = Duration.ofHours(1);

  public AddressDraftResponse saveAddressDraft(UUID draftId, AddressDraftSaveRequest request) {
    log.info("[AddressDraft] 임시 주소 저장 시작 - userId: {}", draftId);

    AddressWithDetailRequest from = request.getFromAddress();
    AddressWithDetailRequest to = request.getToAddress();

    // 출발지와 도착지를 하나로 묶기 위한 객체
    DraftAddressWrapper wrapper = new DraftAddressWrapper(from, to);

    try {
      String json = objectMapper.writeValueAsString(wrapper);
      String key = RedisKeyUtil.draftAddressKey(draftId);

      redisTemplate.opsForValue().set(key, json, TTL);

      log.info("[AddressDraft] 저장 완료 - key: {}, TTL: {}분", key, TTL.toMinutes());

      log.debug("[AddressDraft] 저장된 데이터: {}", json);

      return AddressDraftResponse.builder()
          .roadAddr(from.getAddress().getRoadAddrPart1())
          .build();
    } catch (JsonProcessingException e) {
      log.warn("[AddressDraft] JSON 직렬화 실패 - userId: {}, 이유: {}", draftId, e.getMessage());
      throw new CustomException(AddressErrorCode.ADDRESS_DRAFT_SAVE_FAIL);
    }
  }

  // 출발지 - 도착지 하나로 묶는
  private record DraftAddressWrapper(
      AddressWithDetailRequest fromAddress,
      AddressWithDetailRequest toAddress
  ) {}

// 전체 불러오기 (조회용)
  public AddressFullResponse getAddressDraft(UUID draftId) {
    String redisKey = RedisKeyUtil.draftAddressKey(draftId);
    String json = redisTemplate.opsForValue().get(redisKey);

    if (json == null) {
      throw new CustomException(AddressErrorCode.ADDRESS_DRAFT_NOT_FOUND);
    }

    try {
      DraftAddressWrapper wrapper = objectMapper.readValue(json, DraftAddressWrapper.class);

      return AddressFullResponse.builder()
          .fromAddress(wrapper.fromAddress())
          .toAddress(wrapper.toAddress())
          .build();

    } catch (Exception e) {
      log.warn("[AddressDraft] 역직렬화 실패 - key: {}, 이유: {}", redisKey, e.getMessage());
      throw new CustomException(AddressErrorCode.JSON_PARSE_ERROR);
    }
  }

}

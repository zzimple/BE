package com.zzimple.estimate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.request.AddressDraftSaveRequest;
import com.zzimple.estimate.dto.response.AddressDraftResponse;
import com.zzimple.estimate.entity.Address;
import com.zzimple.global.exception.AddressErrorCode;
import com.zzimple.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateDraftService {

  private final StringRedisTemplate redisTemplate;

  // JSON 파싱을 위한 ObjectMapper
  private final ObjectMapper objectMapper;

  private static final String DRAFT_KEY_PREFIX = "estimate:draft:address:";
  private static final Duration TTL = Duration.ofMinutes(30);

  public AddressDraftResponse saveAddressDraft(Long userId, AddressDraftSaveRequest request) {
    log.info("[AddressDraft] 임시 주소 저장 시작 - userId: {}", userId);

    // 관리 번호 분리해서 저장하도록
    Address fromAddress = request.getFromAddress().toEntity();
    Address toAddress = request.getToAddress().toEntity();

    // 출발지와 도착지를 하나로 묶기 위한 객체
    DraftAddressWrapper wrapper = new DraftAddressWrapper(fromAddress, toAddress);

    try {
      String json = objectMapper.writeValueAsString(wrapper);
      String key = DRAFT_KEY_PREFIX + userId;

      redisTemplate.opsForValue().set(key, json, TTL);

      log.info("[AddressDraft] 저장 완료 - key: {}, TTL: {}분", key, TTL.toMinutes());
      log.debug("[AddressDraft] 저장된 데이터: {}", json);

      return AddressDraftResponse.builder()
          .roadAddr(request.getFromAddress().getRoadAddr())
          .message("도로명 주소 임시 저장이 완료되었습니다.")
          .build();
    } catch (JsonProcessingException e) {
      log.warn("[AddressDraft] JSON 직렬화 실패 - userId: {}, 이유: {}", userId, e.getMessage());
      throw new CustomException(AddressErrorCode.ADDRESS_DRAFT_SAVE_FAIL);
    }
  }

  // 출발지 - 도착지 하나로 묶는
  private record DraftAddressWrapper(Address fromAddress, Address toAddress) {}
}

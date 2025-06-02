package com.zzimple.estimate.guest.service;

import com.zzimple.estimate.guest.dto.request.MoveOptionTypeRequest;
import com.zzimple.estimate.guest.dto.response.MoveOptionTypeResponse;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.estimate.guest.exception.MoveErrorCode;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoveOptionService {

  private final StringRedisTemplate redisTemplate;
  private static final Duration TTL = Duration.ofHours(3);

  public MoveOptionTypeResponse saveMoveOptionType(UUID draftId, MoveOptionTypeRequest request) {
    MoveOptionType optionType = request.getOptionType();

    if (draftId == null || optionType == null) {
      throw new CustomException(MoveErrorCode.INVALID_REQUEST);
    }

    String redisKey = RedisKeyUtil.draftMoveOptionKey(draftId);
    redisTemplate.opsForValue().set(redisKey, optionType.name(), TTL);

    log.info("[MoveService] 소형이사 옵션 저장 완료 - draftId: {}, optionType: {}", draftId, optionType);

    return MoveOptionTypeResponse.builder()
        .optionType(optionType)
        .build();
  }

  // 불러오기
  public MoveOptionTypeResponse getOptionIfExist(UUID draftId) {
    String redisKey = RedisKeyUtil.draftMoveOptionKey(draftId);
    String value = redisTemplate.opsForValue().get(redisKey);

    if (value == null) {
      return null; // 선택 안 한 경우 null 리턴
    }

    return MoveOptionTypeResponse.builder()
        .optionType(MoveOptionType.valueOf(value))
        .build();
  }

}

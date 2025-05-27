package com.zzimple.estimate.service;

import com.zzimple.estimate.dto.request.MoveTypeDraftRequest;
import com.zzimple.estimate.dto.response.MoveTypeResponse;
import com.zzimple.estimate.enums.MoveType;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.estimate.exception.MoveErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoveTypeService {

  private final StringRedisTemplate redisTemplate;

  private static final Duration TTL = Duration.ofMinutes(30); // TTL은 30분 설정

  // 이사 유형 저장
  public MoveTypeResponse saveMoveType(UUID draftId, MoveTypeDraftRequest request) {
    MoveType moveType = request.getMoveType();

    if (draftId == null || moveType == null) {
      throw new CustomException(MoveErrorCode.INVALID_REQUEST);
    }

    String redisKey = RedisKeyUtil.draftMoveTypeKey(draftId);
    redisTemplate.opsForValue().set(redisKey, moveType.name(), TTL);

    log.info("[MoveService] 이사 유형 저장 완료 - draftId: {}, moveType: {}", draftId, moveType);

    return MoveTypeResponse.builder()
        .moveType(request.getMoveType())
        .build();
  }

  // 불러오기
  public MoveTypeResponse getMoveType(UUID draftId) {
    String redisKey = RedisKeyUtil.draftMoveTypeKey(draftId);
    String value = redisTemplate.opsForValue().get(redisKey);

    if (value == null) {
      throw new CustomException(MoveErrorCode.MOVE_TYPE_NOT_FOUND);
    }

    return MoveTypeResponse.builder()
        .moveType(MoveType.valueOf(value))
        .build();
  }

}

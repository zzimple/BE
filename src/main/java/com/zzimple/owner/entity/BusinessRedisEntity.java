package com.zzimple.owner.entity;

import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@RedisHash(value = "BusinessCache", timeToLive = 1800) // 30분 TTL
public class BusinessRedisEntity {
  @Id
  private String businessNumber;

  private String status;
}
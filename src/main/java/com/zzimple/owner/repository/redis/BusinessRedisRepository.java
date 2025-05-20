package com.zzimple.owner.repository.redis;

import com.zzimple.owner.entity.BusinessRedisEntity;
import org.springframework.data.repository.CrudRepository;

public interface BusinessRedisRepository extends CrudRepository<BusinessRedisEntity, String> {
}

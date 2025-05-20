package com.zzimple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableRedisRepositories(basePackages = {
		"com.zzimple.owner.repository.redis"
})
public class ZzimpleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZzimpleApplication.class, args);
	}

}

package com.zzimple.global.sms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class SmsApiProperties {
  @Value("${sms.api.msg-from}")
  private String fromNumber;

  @Value("${sms.api.base-url}")
  private String baseUrl;

  @Value("${sms.api.token}")
  private String token;

  @Value("${sms.api.app-service-seq}")
  private String appServiceSeq;

  @Value("${sms.api.api-version}")
  private String apiVersion;
}
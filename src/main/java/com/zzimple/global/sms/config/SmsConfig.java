package com.zzimple.global.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SmsConfig {
  @Bean
  public WebClient smsWebClient(SmsApiProperties prop) {
    return WebClient.builder()
        .baseUrl(prop.getBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + prop.getToken())
        .defaultHeader("appServiceSeq", prop.getAppServiceSeq())
        .defaultHeader("apiVersion", prop.getApiVersion())
        .build();
  }
}
package com.zzimple.global.sms.service.util;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class SmsCertificationUtil {

  private static final String KEY_PREFIX = "signup:code:";


  // 인증번호 생성
  public String createRandomCode() {
    return String.valueOf(100000 + new Random().nextInt(900000));
  }

  // redis 생성
  public String buildKey(String phoneNumber) {
    return KEY_PREFIX + phoneNumber;
  }
}
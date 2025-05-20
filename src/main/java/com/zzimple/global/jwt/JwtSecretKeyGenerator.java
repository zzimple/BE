package com.zzimple.global.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtSecretKeyGenerator {
  public static void main(String[] args) {
    SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // HS256 알고리즘에 맞는 SecretKey 생성
    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    System.out.println("Generated Secret Key: " + encodedKey);
  }
}
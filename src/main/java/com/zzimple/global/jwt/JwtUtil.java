package com.zzimple.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
  @Value("${jwt.secret-key}")
  private String SECRET_KEY;

  // access token 만료시간 - 1시간
  private static final long ACCESS_TOKEN_EXPIRE_TIME = TimeUnit.HOURS.toMillis(1);

  // refresh token
  private static final long REFRESH_TOKEN_EXPIRE_TIME = TimeUnit.DAYS.toMillis(30);

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
  }

  // 일반 사용자용 storeId 없는 버전
  public String createAccessToken(String loginId, List<String> roles) {
    Date now = new Date();
    Date expireTime = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
    Key key = getSigningKey();

    return Jwts.builder()
        .setSubject(loginId)
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expireTime)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // Access Token 발급 부분 - 사장
  public String createAccessToken(String loginId, List<String> roles, Long storeId, Long ownerId) {
    Date now = new Date();
    Date expireTime = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
    Key key = getSigningKey();

    return Jwts.builder()
        .setSubject(loginId)
        .claim("roles", roles)
        .claim("storeId", storeId)
        .claim("ownerId", ownerId)
        .setIssuedAt(new Date())
        .setExpiration(expireTime)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // Refresh Token 발급 부분
  public String createRefreshToken(String userId) {
    Date now = new Date();
    Date expireTime = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);
    Key key = getSigningKey();

    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(expireTime)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public long getRefreshTokenExpirySeconds() {
    return TimeUnit.MILLISECONDS.toSeconds(REFRESH_TOKEN_EXPIRE_TIME);
  }

  // 토큰 검증 부분
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

  // JWT 토큰 Claims에서 모든 정보 추출
  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // 토큰에서 loginId 추출
  public String extractLoginId(String token) {
    return getAllClaimsFromToken(token).getSubject();
  }

  // 토큰에서 storeId 추출
  public Long extractStoreId(String token) {
    Claims claims = getAllClaimsFromToken(token);
    Object storeId = claims.get("storeId");

    if (storeId == null) {
      return null;
    }

    if (storeId instanceof Integer) {
      return ((Integer) storeId).longValue();
    } else if (storeId instanceof Long) {
      return (Long) storeId;
    } else if (storeId instanceof String) {
      try {
        return Long.parseLong((String) storeId);
      } catch (NumberFormatException e) {
        log.warn("storeId 파싱 실패: {}", storeId);
        return null;
      }
    } else {
      log.warn("예상치 못한 storeId 타입: {}", storeId.getClass());
      return null;
    }
  }


  // 토큰에서 만료 시간 추출
  public Date extractExpiration(String token) {
    return getAllClaimsFromToken(token).getExpiration();
  }

  // 토큰이 만료되었는지 확인
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }
}
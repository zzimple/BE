package com.zzimple.global.jwt;

import com.zzimple.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
  private final Long userId;
  private final String username;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;
  private final Long storeId;
  private final Long ownerId;

  // 일반 사용자 (고객, 직원)용 생성자
  public CustomUserDetails(User user) {
    this.userId = user.getId();
    this.username = user.getUserName();
    this.password = user.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    this.storeId = null;
    this.ownerId = null;
  }

  public CustomUserDetails(User user, Long storeId) {
    this.userId = user.getId();
    this.username = user.getUserName();
    this.password = user.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    this.storeId = storeId;
    this.ownerId = null;
  }

  public CustomUserDetails(User user, Long storeId, Long ownerId) {
    this.userId = user.getId();
    this.username = user.getUserName();
    this.password = user.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
    );
    this.storeId = storeId;
    this.ownerId = ownerId;
  }
}
package com.zzimple.user.entity;

import com.zzimple.global.common.BaseTimeEntity;
import com.zzimple.user.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "user_name", nullable = false)
  private String userName; // 이름

  @Column(name = "phone_number", nullable = false, unique = true)
  private String phoneNumber; // 전화번호

  @Column(name = "email", unique = true)
  private String email; // 이메일

  @Column(name = "login_id", nullable = false, unique = true)
  private String loginId; // 아이디

  @Column(name = "password")
  private String password;  // 비밀번호

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Column(name = "refresh_token")
  private String refreshToken;
}
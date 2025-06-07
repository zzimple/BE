package com.zzimple.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "owners",
    indexes = {
        @Index(name = "idx_owner_user_id", columnList = "user_id"),
        @Index(name = "idx_owner_store_id", columnList = "store_id")
    })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Owner extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, unique = true)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

//  private Long storeId;

  @Column(name = "business_number", nullable = false, unique = true)
  private String businessNumber;

  @Column(name = "insured")
//  @Column(name = "insured", nullable = false)
  private Boolean insured;

  @Column(name = "status")
  private String status;

  @Column(name = "road_full_addr", nullable = false)
  private String roadFullAddr; // 전체 도로명 주소 (예: 서울 강남구 테헤란로 123)

  @Column(name = "road_addr", nullable = false)
  private String roadAddrPart1;        // 도로명 주소 요약 (예: 테헤란로)

  @Column(name = "addr_detail")
  private String addrDetail; // 상세 주소 (예: 101동 203호)

  @Column(name = "zip_no", nullable = false)
  private String zipNo; // 우편번호
}
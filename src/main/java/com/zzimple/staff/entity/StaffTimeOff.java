package com.zzimple.staff.entity;

import com.zzimple.global.common.BaseTimeEntity;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.enums.TimeOffType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "staff_time_off")
public class StaffTimeOff extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long staffTimeOffId;

  // 직원 식별용 인덱스
  private Long ownerId;

  private Long staffId;

  private String staffName;

  // 소속 사장님 식별용 인덱스
  private Long storeId;

  private LocalDate startDate;
  private LocalDate endDate;

  private TimeOffType type;

  private String reason;

  @Enumerated(EnumType.STRING)
  private Status status;
}

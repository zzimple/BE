package com.zzimple.estimate.owner.entity;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateResponse {
    @Id
    @GeneratedValue
    private Long id;

    private Long estimateNo;
    private Long storeId;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status; // ACCEPTED or REJECTED

    private LocalDateTime respondedAt;

    // ✅ 내가 응답했는지 여부 (백엔드 계산 후 응답 DTO에 추가용, DB에는 저장하지 않음)
    private transient boolean respondedByMe;
}

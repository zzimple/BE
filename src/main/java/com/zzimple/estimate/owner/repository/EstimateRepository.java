package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {

  @Query(
      value = """
      SELECT *
        FROM estimate
       WHERE status = 'WAITING'
         -- 연도 필터
         AND (:year       IS NULL OR LEFT(move_date, 4)    = :year)
         -- 월 필터
         AND (:month      IS NULL OR SUBSTR(move_date, 5,2) = :month)
         -- 일 필터
         AND (:day        IS NULL OR RIGHT(move_date, 2)   = :day)
         -- 이사 유형 필터
         AND (:moveType   IS NULL OR move_type = :moveType)
         -- 출발지 필터
         AND (:fromRegion1 IS NULL OR SUBSTRING_INDEX(from_road_full_addr, ' ', 1)  = :fromRegion1)
         AND (:fromRegion2 IS NULL OR SUBSTRING_INDEX(                                     
               SUBSTRING_INDEX(from_road_full_addr, ' ', 2), ' ', -1) = :fromRegion2)
         -- 도착지 필터
         AND (:toRegion1   IS NULL OR SUBSTRING_INDEX(to_road_full_addr, ' ', 1)    = :toRegion1)
         AND (:toRegion2   IS NULL OR SUBSTRING_INDEX(                                     
               SUBSTRING_INDEX(to_road_full_addr, ' ', 2), ' ', -1)   = :toRegion2)
     ORDER BY move_time DESC
    """,
      countQuery = """
      SELECT COUNT(*)
        FROM estimate
       WHERE status = 'WAITING'
         AND (:year       IS NULL OR LEFT(move_date, 4)    = :year)
         AND (:month      IS NULL OR SUBSTR(move_date, 5,2) = :month)
         AND (:day        IS NULL OR RIGHT(move_date, 2)   = :day)
         AND (:moveType   IS NULL OR move_type = :moveType)
         AND (:moveOption IS NULL OR option_type = :moveOption)
         AND (:fromRegion1 IS NULL OR SUBSTRING_INDEX(from_road_full_addr, ' ', 1)  = :fromRegion1)
         AND (:fromRegion2 IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(from_road_full_addr, ' ', 2), ' ', -1) = :fromRegion2)
         AND (:toRegion1   IS NULL OR SUBSTRING_INDEX(to_road_full_addr, ' ', 1)    = :toRegion1)
         AND (:toRegion2   IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(to_road_full_addr, ' ', 2), ' ', -1)   = :toRegion2)
    """,
      nativeQuery = true
  )
  Page<Estimate> findPublicEstimatesWithFilters(
      @Param("year")        String year,
      @Param("month")       String month,
      @Param("day")         String day,
      @Param("moveType")    String moveType,
      @Param("moveOption")  String moveOption,
      @Param("fromRegion1") String fromRegion1,
      @Param("fromRegion2") String fromRegion2,
      @Param("toRegion1")   String toRegion1,
      @Param("toRegion2")   String toRegion2,
      Pageable pageable
  );

  Page<Estimate> findByUserIdAndStatus(Long userId, EstimateStatus status, Pageable pageable);

  @Query(
      value = """
      SELECT *
        FROM estimate
       WHERE status      = 'ACCEPTED'
         AND store_id    = :storeId
         -- 연도 필터
         AND (:year       IS NULL OR LEFT(move_date, 4)    = :year)
         -- 월 필터
         AND (:month      IS NULL OR SUBSTR(move_date, 5,2) = :month)
         -- 일 필터
         AND (:day        IS NULL OR RIGHT(move_date, 2)   = :day)
         -- 이사 유형 필터
         AND (:moveType   IS NULL OR move_type   = :moveType)
         -- 옵션 타입 필터
         AND (:moveOption IS NULL OR option_type = :moveOption)
         -- 출발지 필터
         AND (:fromRegion1 IS NULL OR SUBSTRING_INDEX(from_road_full_addr, ' ', 1) = :fromRegion1)
         AND (:fromRegion2 IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(from_road_full_addr, ' ', 2), ' ', -1)      = :fromRegion2)
         -- 도착지 필터
         AND (:toRegion1   IS NULL OR SUBSTRING_INDEX(to_road_full_addr,   ' ', 1) = :toRegion1)
         AND (:toRegion2   IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(to_road_full_addr,   ' ', 2), ' ', -1)      = :toRegion2)
       ORDER BY move_time DESC
      """,
      countQuery = """
      SELECT COUNT(*)
        FROM estimate
       WHERE status      = 'ACCEPTED'
         AND store_id    = :storeId
         AND (:year       IS NULL OR LEFT(move_date, 4)    = :year)
         AND (:month      IS NULL OR SUBSTR(move_date, 5,2) = :month)
         AND (:day        IS NULL OR RIGHT(move_date, 2)   = :day)
         AND (:moveType   IS NULL OR move_type   = :moveType)
         AND (:moveOption IS NULL OR option_type = :moveOption)
         AND (:fromRegion1 IS NULL OR SUBSTRING_INDEX(from_road_full_addr, ' ', 1) = :fromRegion1)
         AND (:fromRegion2 IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(from_road_full_addr, ' ', 2), ' ', -1)      = :fromRegion2)
         AND (:toRegion1   IS NULL OR SUBSTRING_INDEX(to_road_full_addr,   ' ', 1) = :toRegion1)
         AND (:toRegion2   IS NULL OR SUBSTRING_INDEX(
               SUBSTRING_INDEX(to_road_full_addr,   ' ', 2), ' ', -1)      = :toRegion2)
      """,
      nativeQuery = true
  )
  Page<Estimate> findAcceptedEstimatesWithFilters(
      @Param("storeId")      Long storeId,
      @Param("year")         String year,
      @Param("month")        String month,
      @Param("day")          String day,
      @Param("moveType")     String moveType,
      @Param("moveOption")   String moveOption,
      @Param("fromRegion1")  String fromRegion1,
      @Param("fromRegion2")  String fromRegion2,
      @Param("toRegion1")    String toRegion1,
      @Param("toRegion2")    String toRegion2,
      Pageable pageable
  );
}

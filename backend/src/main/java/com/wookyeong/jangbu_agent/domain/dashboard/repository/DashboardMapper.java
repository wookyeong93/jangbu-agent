package com.wookyeong.jangbu_agent.domain.dashboard.repository;

import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlySummaryResult;
import com.wookyeong.jangbu_agent.domain.dashboard.dto.MonthlyTrendRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 대시보드용 통계 쿼리 매퍼 (MyBatis).
 * GROUP BY 집계가 포함되므로 JPA 대신 MyBatis 사용 (backend CLAUDE.md 영속성 기준).
 * ledger 도메인에 의존하지 않고 tb_ledger를 직접 집계한다 (guide 도메인과 동일한 패턴).
 */
@Mapper
public interface DashboardMapper {

    /** 기간(보통 당월) 매입/매출/지출/순익 합계. 데이터 없으면 0. */
    MonthlySummaryResult getSummaryByPeriod(
            @Param("userNo") Integer userNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** 기간 내 월별(year, month 기준 GROUP BY) 집계. 활동 있는 달만 행 반환, 연·월 오름차순. */
    List<MonthlyTrendRow> getMonthlyTrend(
            @Param("userNo") Integer userNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

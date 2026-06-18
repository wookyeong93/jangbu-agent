package com.wookyeong.jangbu_agent.domain.guide.repository;

import com.wookyeong.jangbu_agent.domain.guide.dto.PeriodSummaryResult;
import com.wookyeong.jangbu_agent.domain.guide.dto.PurchaseCycleRow;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 가이드 생성용 통계 쿼리 매퍼 (MyBatis).
 * 윈도우 함수·복잡 집계가 포함되므로 JPA 대신 MyBatis 사용 (backend CLAUDE.md 영속성 기준).
 */
@Mapper
public interface GuideAnalysisMapper {

    /** 기간별 매입/매출/지출/순익 합계. */
    PeriodSummaryResult getSummaryByPeriod(
            @Param("userNo") Integer userNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** 요일별 평균 매출 (최근 8주). avg_sale 내림차순 정렬. */
    List<WeekdaySalesResult> getWeekdaySalesTrend(@Param("userNo") Integer userNo);

    /**
     * 최근 10건 매입 날짜와 직전 매입 대비 경과 일수.
     * 서비스 레이어에서 평균 주기·다음 예상 매입일을 계산한다.
     */
    List<PurchaseCycleRow> getPurchaseCycleRows(@Param("userNo") Integer userNo);
}

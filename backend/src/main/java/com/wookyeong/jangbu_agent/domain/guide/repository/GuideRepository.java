package com.wookyeong.jangbu_agent.domain.guide.repository;

import com.wookyeong.jangbu_agent.domain.guide.entity.DailyGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 데일리 가이드 JPA 리포지토리.
 * 통계 쿼리는 {@link GuideAnalysisMapper} (MyBatis) 가 담당한다.
 */
public interface GuideRepository extends JpaRepository<DailyGuide, Long> {

    Optional<DailyGuide> findByUserUserNoAndGuideDt(Integer userNo, LocalDate guideDt);
}

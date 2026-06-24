package com.wookyeong.jangbu_agent.domain.code.repository;

import com.wookyeong.jangbu_agent.domain.code.entity.Code;
import com.wookyeong.jangbu_agent.domain.code.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, CodeId> {

    /** 사용 중인 코드 전체. 그룹별로 묶을 때 결과를 group_code 기준으로 재정렬해 쓴다. */
    List<Code> findByUseYnOrderBySortOrderAsc(String useYn);

    List<Code> findById_GroupCodeAndUseYnOrderBySortOrderAsc(String groupCode, String useYn);
}

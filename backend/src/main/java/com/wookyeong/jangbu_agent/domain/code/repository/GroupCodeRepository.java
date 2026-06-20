package com.wookyeong.jangbu_agent.domain.code.repository;

import com.wookyeong.jangbu_agent.domain.code.entity.GroupCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupCodeRepository extends JpaRepository<GroupCode, String> {

    List<GroupCode> findByUseYnOrderBySortOrderAsc(String useYn);
}

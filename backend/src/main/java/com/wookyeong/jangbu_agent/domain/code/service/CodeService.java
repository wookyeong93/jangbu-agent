package com.wookyeong.jangbu_agent.domain.code.service;

import com.wookyeong.jangbu_agent.domain.code.dto.CodeResponse;
import com.wookyeong.jangbu_agent.domain.code.dto.GroupCodeResponse;
import com.wookyeong.jangbu_agent.domain.code.entity.Code;
import com.wookyeong.jangbu_agent.domain.code.entity.GroupCode;
import com.wookyeong.jangbu_agent.domain.code.repository.CodeRepository;
import com.wookyeong.jangbu_agent.domain.code.repository.GroupCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공통 코드(tb_group_code, tb_code) 조회 로직.
 *
 * <p>사용 중(use_yn = 'Y')인 코드만 반환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private static final String USE_Y = "Y";

    private final GroupCodeRepository groupCodeRepository;
    private final CodeRepository codeRepository;

    /** 전체 그룹 코드 + 그룹별 하위 코드 목록. 그룹·코드 모두 sortOrder 오름차순. */
    public List<GroupCodeResponse> findAll() {
        List<GroupCode> groups = groupCodeRepository.findByUseYnOrderBySortOrderAsc(USE_Y);
        List<Code> codes = codeRepository.findByUseYnOrderBySortOrderAsc(USE_Y);

        Map<String, List<CodeResponse>> codesByGroup = codes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getId().getGroupCode(),
                        Collectors.mapping(CodeResponse::from, Collectors.toList())));

        return groups.stream()
                .map(g -> GroupCodeResponse.of(g, codesByGroup.getOrDefault(g.getGroupCode(), List.of())))
                .toList();
    }

    /** 특정 그룹의 하위 코드 목록. 그룹이 없거나 하위 코드가 없으면 빈 리스트 (404 아님 — 프론트가 단일 분기로 처리). */
    public List<CodeResponse> findByGroupCode(String groupCode) {
        return codeRepository.findById_GroupCodeAndUseYnOrderBySortOrderAsc(groupCode, USE_Y).stream()
                .map(CodeResponse::from)
                .toList();
    }
}

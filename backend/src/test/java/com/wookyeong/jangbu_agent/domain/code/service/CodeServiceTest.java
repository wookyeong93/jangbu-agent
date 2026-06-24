package com.wookyeong.jangbu_agent.domain.code.service;

import com.wookyeong.jangbu_agent.domain.code.dto.CodeResponse;
import com.wookyeong.jangbu_agent.domain.code.dto.GroupCodeResponse;
import com.wookyeong.jangbu_agent.domain.code.entity.Code;
import com.wookyeong.jangbu_agent.domain.code.entity.CodeId;
import com.wookyeong.jangbu_agent.domain.code.entity.GroupCode;
import com.wookyeong.jangbu_agent.domain.code.repository.CodeRepository;
import com.wookyeong.jangbu_agent.domain.code.repository.GroupCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeService 단위 테스트")
class CodeServiceTest {

    @Mock GroupCodeRepository groupCodeRepository;
    @Mock CodeRepository codeRepository;

    @InjectMocks CodeService codeService;

    @Test
    @DisplayName("전체 조회 — 그룹별로 하위 코드가 sortOrder 순으로 묶인다")
    void findAll_groupsCodesBySortOrder() {
        GroupCode trxType = groupCode("TRX_TYPE", "거래 구분", 1);
        when(groupCodeRepository.findByUseYnOrderBySortOrderAsc("Y")).thenReturn(List.of(trxType));
        when(codeRepository.findByUseYnOrderBySortOrderAsc("Y")).thenReturn(List.of(
                code("TRX_TYPE", "PURCHASE", "매입", 1),
                code("TRX_TYPE", "SALE", "매출", 2),
                code("TRX_TYPE", "EXPENSE", "지출", 3)
        ));

        List<GroupCodeResponse> result = codeService.findAll();

        assertThat(result).hasSize(1);
        GroupCodeResponse group = result.get(0);
        assertThat(group.getGroupCode()).isEqualTo("TRX_TYPE");
        assertThat(group.getCodes()).extracting(CodeResponse::getCode)
                .containsExactly("PURCHASE", "SALE", "EXPENSE");
    }

    @Test
    @DisplayName("전체 조회 — 하위 코드가 없는 그룹은 빈 리스트")
    void findAll_groupWithNoCodes_returnsEmptyList() {
        when(groupCodeRepository.findByUseYnOrderBySortOrderAsc("Y"))
                .thenReturn(List.of(groupCode("EMPTY_GROUP", "빈 그룹", 1)));
        when(codeRepository.findByUseYnOrderBySortOrderAsc("Y")).thenReturn(List.of());

        List<GroupCodeResponse> result = codeService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodes()).isEmpty();
    }

    @Test
    @DisplayName("그룹별 조회 — 존재하는 그룹이면 하위 코드 목록 반환")
    void findByGroupCode_existingGroup_returnsCodes() {
        when(codeRepository.findById_GroupCodeAndUseYnOrderBySortOrderAsc("TRX_TYPE", "Y"))
                .thenReturn(List.of(code("TRX_TYPE", "PURCHASE", "매입", 1)));

        List<CodeResponse> result = codeService.findByGroupCode("TRX_TYPE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PURCHASE");
    }

    @Test
    @DisplayName("그룹별 조회 — 존재하지 않는 그룹이면 빈 리스트 (404 아님, 컨트롤러가 200+메시지로 처리)")
    void findByGroupCode_unknownGroup_returnsEmptyList() {
        when(codeRepository.findById_GroupCodeAndUseYnOrderBySortOrderAsc("UNKNOWN", "Y"))
                .thenReturn(List.of());

        List<CodeResponse> result = codeService.findByGroupCode("UNKNOWN");

        assertThat(result).isEmpty();
    }

    private GroupCode groupCode(String groupCode, String groupName, int sortOrder) {
        return GroupCode.builder()
                .groupCode(groupCode)
                .groupName(groupName)
                .sortOrder(sortOrder)
                .useYn("Y")
                .build();
    }

    private Code code(String groupCode, String code, String codeName, int sortOrder) {
        return Code.builder()
                .id(new CodeId(groupCode, code))
                .codeName(codeName)
                .sortOrder(sortOrder)
                .useYn("Y")
                .build();
    }
}

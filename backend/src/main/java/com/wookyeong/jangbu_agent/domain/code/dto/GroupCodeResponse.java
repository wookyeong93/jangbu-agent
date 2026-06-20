package com.wookyeong.jangbu_agent.domain.code.dto;

import com.wookyeong.jangbu_agent.domain.code.entity.GroupCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "그룹 코드 + 하위 코드 목록 응답")
public class GroupCodeResponse {

    @Schema(description = "그룹 코드 값", example = "TRX_TYPE")
    private String groupCode;

    @Schema(description = "그룹 코드 명", example = "거래 구분")
    private String groupName;

    @Schema(description = "하위 코드 목록 (sortOrder 오름차순)")
    private List<CodeResponse> codes;

    public static GroupCodeResponse of(GroupCode groupCode, List<CodeResponse> codes) {
        return GroupCodeResponse.builder()
                .groupCode(groupCode.getGroupCode())
                .groupName(groupCode.getGroupName())
                .codes(codes)
                .build();
    }
}

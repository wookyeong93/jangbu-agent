package com.wookyeong.jangbu_agent.domain.code.dto;

import com.wookyeong.jangbu_agent.domain.code.entity.Code;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "코드 응답")
public class CodeResponse {

    @Schema(description = "코드 값", example = "PURCHASE")
    private String code;

    @Schema(description = "코드 명", example = "매입")
    private String codeName;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    public static CodeResponse from(Code code) {
        return CodeResponse.builder()
                .code(code.getId().getCode())
                .codeName(code.getCodeName())
                .sortOrder(code.getSortOrder())
                .build();
    }
}

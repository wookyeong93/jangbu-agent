package com.wookyeong.jangbu_agent.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/** 내 프로필 조회 응답. */
@Getter
@Builder
@Schema(description = "내 프로필 응답")
public class UserResponse {

    @Schema(description = "사용자 아이디", example = "user01")
    private String userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userNm;
}

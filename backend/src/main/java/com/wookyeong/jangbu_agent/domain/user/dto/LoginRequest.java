package com.wookyeong.jangbu_agent.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 로그인 요청. */
@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "사용자 아이디", example = "user01")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", example = "Pass1234!")
    private String password;
}

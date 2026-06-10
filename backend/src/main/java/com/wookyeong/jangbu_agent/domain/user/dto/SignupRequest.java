package com.wookyeong.jangbu_agent.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원가입 요청. 입력 규칙은 docs/policy/user.md 참고. */
@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
    @Schema(description = "사용자 아이디 (영문·숫자, 최대 50자)", example = "user01")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?\\\\|]).+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
    )
    @Schema(description = "비밀번호 (영문·숫자·특수문자 각 1자 이상, 8자 이상)", example = "Pass1234!")
    private String password;

    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    @Schema(description = "사용자 이름 (선택, 최대 100자)", example = "홍길동")
    private String userNm;
}

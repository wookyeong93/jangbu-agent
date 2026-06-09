package com.wookyeong.jangbu_agent.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원 정보 수정 요청. userNm·newPassword 중 하나 이상 포함해야 한다. */
@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String userNm;

    private String currentPassword;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?\\\\|]).+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
    )
    private String newPassword;
}

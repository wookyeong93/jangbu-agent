package com.wookyeong.jangbu_agent.domain.user.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.user.dto.UpdateProfileRequest;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    // ── 이름 변경 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이름 변경 성공")
    void updateProfile_nameOnly_success() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded").userNm("홍길동").build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));

        UpdateProfileRequest request = makeRequest("이순신", null, null);
        userService.updateProfile(1, request);

        assertThat(user.getUserNm()).isEqualTo("이순신");
    }

    // ── 비밀번호 변경 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updateProfile_passwordOnly_success() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded-old").build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("old1!", "encoded-old")).willReturn(true);
        given(passwordEncoder.matches("new1!", "encoded-old")).willReturn(false);
        given(passwordEncoder.encode("new1!")).willReturn("encoded-new");

        UpdateProfileRequest request = makeRequest(null, "old1!", "new1!");
        userService.updateProfile(1, request);

        assertThat(user.getUserPwd()).isEqualTo("encoded-new");
    }

    @Test
    @DisplayName("비밀번호 변경 — 현재 비밀번호 누락 시 INVALID_INPUT")
    void updateProfile_missingCurrentPassword_throwsException() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded-old").build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));

        UpdateProfileRequest request = makeRequest(null, null, "new1!");

        assertThatThrownBy(() -> userService.updateProfile(1, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("비밀번호 변경 — 현재 비밀번호 불일치 시 INVALID_PASSWORD")
    void updateProfile_wrongCurrentPassword_throwsException() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded-old").build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong!", "encoded-old")).willReturn(false);

        UpdateProfileRequest request = makeRequest(null, "wrong!", "new1!");

        assertThatThrownBy(() -> userService.updateProfile(1, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 — 새 비밀번호가 현재와 동일하면 SAME_AS_CURRENT_PASSWORD")
    void updateProfile_samePassword_throwsException() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded-old").build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("old1!", "encoded-old")).willReturn(true);

        UpdateProfileRequest request = makeRequest(null, "old1!", "old1!");

        assertThatThrownBy(() -> userService.updateProfile(1, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SAME_AS_CURRENT_PASSWORD);
    }

    // ── 공통 예외 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("변경 항목이 없으면 INVALID_INPUT")
    void updateProfile_noFields_throwsException() {
        UpdateProfileRequest request = makeRequest(null, null, null);

        assertThatThrownBy(() -> userService.updateProfile(1, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND")
    void updateProfile_userNotFound_throwsException() {
        given(userRepository.findById(99)).willReturn(Optional.empty());

        UpdateProfileRequest request = makeRequest("홍길동", null, null);

        assertThatThrownBy(() -> userService.updateProfile(99, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private UpdateProfileRequest makeRequest(String userNm, String currentPassword, String newPassword) {
        UpdateProfileRequest req = new UpdateProfileRequest();
        setField(req, "userNm", userNm);
        setField(req, "currentPassword", currentPassword);
        setField(req, "newPassword", newPassword);
        return req;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

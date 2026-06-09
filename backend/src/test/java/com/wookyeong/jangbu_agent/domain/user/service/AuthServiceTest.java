package com.wookyeong.jangbu_agent.domain.user.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.user.dto.LoginRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.SignupRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.TokenResponse;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import com.wookyeong.jangbu_agent.infra.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks AuthService authService;

    // ── 회원가입 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 성공 — 비밀번호는 인코딩되어 저장된다")
    void signup_success() {
        given(userRepository.existsByUserId("user01")).willReturn(false);
        given(passwordEncoder.encode("password1!")).willReturn("encoded");

        SignupRequest request = new SignupRequest();
        setField(request, "userId", "user01");
        setField(request, "password", "password1!");
        setField(request, "userNm", "홍길동");

        authService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUserPwd()).isEqualTo("encoded");
    }

    @Test
    @DisplayName("회원가입 — 중복 아이디면 USER_ID_DUPLICATED 예외")
    void signup_duplicateUserId_throwsException() {
        given(userRepository.existsByUserId("user01")).willReturn(true);

        SignupRequest request = new SignupRequest();
        setField(request, "userId", "user01");
        setField(request, "password", "password1!");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_ID_DUPLICATED);

        verify(userRepository, never()).save(any());
    }

    // ── 로그인 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공 — Refresh Token 은 SHA-256 해시로 저장된다")
    void login_success_refreshTokenIsHashed() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded").build();
        given(userRepository.findByUserId("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1!", "encoded")).willReturn(true);
        given(jwtTokenProvider.generateRefreshToken()).willReturn("raw-uuid");
        given(jwtTokenProvider.hashToken("raw-uuid")).willReturn("hashed-uuid");
        given(jwtTokenProvider.generateAccessToken("user01", 1)).willReturn("access-token");

        LoginRequest request = new LoginRequest();
        setField(request, "userId", "user01");
        setField(request, "password", "password1!");

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("raw-uuid"); // 클라이언트에는 원본 반환
        assertThat(user.getRefreshToken()).isEqualTo("hashed-uuid");  // DB에는 해시 저장
    }

    @Test
    @DisplayName("로그인 — 존재하지 않는 아이디면 USER_NOT_FOUND 예외")
    void login_userNotFound_throwsException() {
        given(userRepository.findByUserId("ghost")).willReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        setField(request, "userId", "ghost");
        setField(request, "password", "password1!");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인 — 비밀번호 불일치면 INVALID_PASSWORD 예외")
    void login_invalidPassword_throwsException() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded").build();
        given(userRepository.findByUserId("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpw", "encoded")).willReturn(false);

        LoginRequest request = new LoginRequest();
        setField(request, "userId", "user01");
        setField(request, "password", "wrongpw");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    // ── 재발급 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("재발급 성공 — 새 Access Token 반환, Refresh Token 은 유지")
    void reissue_success() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded").build();
        given(jwtTokenProvider.hashToken("raw-uuid")).willReturn("hashed-uuid");
        given(userRepository.findByRefreshToken("hashed-uuid")).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken("user01", 1)).willReturn("new-access-token");

        TokenResponse response = authService.reissue("raw-uuid");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("raw-uuid");
    }

    @Test
    @DisplayName("재발급 — 유효하지 않은 Refresh Token 이면 INVALID_TOKEN 예외")
    void reissue_invalidToken_throwsException() {
        given(jwtTokenProvider.hashToken(anyString())).willReturn("hashed");
        given(userRepository.findByRefreshToken("hashed")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue("invalid-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    // ── 로그아웃 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 — DB 의 Refresh Token 이 null 로 초기화된다")
    void logout_clearsRefreshToken() {
        User user = User.builder().userNo(1).userId("user01").userPwd("encoded")
                .refreshToken("hashed-uuid").build();
        given(userRepository.findByUserId("user01")).willReturn(Optional.of(user));

        authService.logout("user01");

        assertThat(user.getRefreshToken()).isNull();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    /** DTO 의 private 필드를 리플렉션으로 설정 (Setter 없는 Request 객체용). */
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

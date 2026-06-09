package com.wookyeong.jangbu_agent.domain.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SignupRequest 입력 검증 단위 테스트")
class SignupRequestTest {

    static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── 아이디 ────────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "유효한 아이디: {0}")
    @ValueSource(strings = {"user01", "HONG123", "abc", "A1B2C3"})
    @DisplayName("아이디 — 영문+숫자 조합은 통과")
    void userId_valid(String userId) {
        Set<ConstraintViolation<SignupRequest>> violations = validate(userId, "Password1!");
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest(name = "유효하지 않은 아이디: {0}")
    @ValueSource(strings = {"홍길동", "user_01", "user 01", "user@1", "user-1"})
    @DisplayName("아이디 — 한글·특수문자·공백은 거부")
    void userId_invalid(String userId) {
        Set<ConstraintViolation<SignupRequest>> violations = validate(userId, "Password1!");
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    // ── 비밀번호 ──────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "유효한 비밀번호: {0}")
    @ValueSource(strings = {"Password1!", "abc123!@#", "Test1@test", "Aa1!Bb2@"})
    @DisplayName("비밀번호 — 영문+숫자+특수문자 조합은 통과")
    void password_valid(String password) {
        Set<ConstraintViolation<SignupRequest>> violations = validate("user01", password);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest(name = "유효하지 않은 비밀번호: {0}")
    @ValueSource(strings = {
        "password1",   // 특수문자 없음
        "Password!",   // 숫자 없음
        "12345678!",   // 영문 없음
        "Pass1!"       // 8자 미만
    })
    @DisplayName("비밀번호 — 필수 조합 미충족 또는 길이 부족은 거부")
    void password_invalid(String password) {
        Set<ConstraintViolation<SignupRequest>> violations = validate("user01", password);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private Set<ConstraintViolation<SignupRequest>> validate(String userId, String password) {
        try {
            SignupRequest request = new SignupRequest();
            var f1 = SignupRequest.class.getDeclaredField("userId");
            var f2 = SignupRequest.class.getDeclaredField("password");
            f1.setAccessible(true); f1.set(request, userId);
            f2.setAccessible(true); f2.set(request, password);
            return validator.validate(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

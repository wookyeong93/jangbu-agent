package com.wookyeong.jangbu_agent.common.exception;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ConstraintViolationException (@RequestParam @Min/@Max 위반) — 400 응답")
    void handleConstraintViolationException_returns400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("months: 1 이상이어야 합니다");
        ConstraintViolationException e = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolationException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getMessage()).contains("months");
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException (쿼리 파라미터 타입 불일치) — 400 응답")
    void handleTypeMismatchException_returns400() {
        MethodParameter param = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException e =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "months", param, null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatchException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getMessage()).contains("months");
    }
}

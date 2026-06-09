package com.wookyeong.jangbu_agent.common.exception;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 컨트롤러 전역 예외 처리.
 *
 * <p>처리 우선순위:
 * <ol>
 *   <li>{@link BusinessException} — 도메인 규칙 위반. ErrorCode 의 HTTP 상태로 응답.
 *   <li>{@link MethodArgumentNotValidException} — @Valid 검증 실패. 필드별 메시지를 합쳐 400 응답.
 *   <li>{@link Exception} — 그 외 모든 예외. 상세 내용은 로그에만 남기고 500 응답.
 * </ol>
 *
 * <p>컨트롤러에서 try-catch 로 직접 잡지 말 것. 예외를 그냥 던지면 여기서 처리한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 스택 트레이스는 로그에만. 클라이언트에는 C500 고정 메시지만 반환.
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}

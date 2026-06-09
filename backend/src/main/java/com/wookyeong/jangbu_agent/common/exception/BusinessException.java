package com.wookyeong.jangbu_agent.common.exception;

import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import lombok.Getter;

/**
 * 서비스 비즈니스 규칙 위반을 표현하는 예외 베이스 클래스.
 *
 * <p>모든 도메인 예외는 이 클래스를 상속하거나 직접 사용한다.
 * {@link GlobalExceptionHandler} 가 이를 잡아 {@link ErrorCode} 의 HTTP 상태로 응답한다.
 *
 * <p>사용 예:
 * <pre>
 * // ErrorCode 기본 메시지 그대로
 * throw new BusinessException(ErrorCode.LEDGER_NOT_FOUND);
 *
 * // 메시지를 동적으로 구성해야 할 때
 * throw new BusinessException(ErrorCode.LEDGER_ACCESS_DENIED, "장부 ID: " + ledgerId);
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

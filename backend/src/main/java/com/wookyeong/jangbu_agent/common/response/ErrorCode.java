package com.wookyeong.jangbu_agent.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전체 에러 코드 정의.
 *
 * <p>코드 체계:
 * <ul>
 *   <li>C - 공통 (입력 오류, 인증, 서버 에러)
 *   <li>U - 사용자/인증 도메인
 *   <li>L - 장부(매입·매출·지출) 도메인
 * </ul>
 *
 * <p>새 도메인 추가 시 이 파일에 섹션을 추가하고 prefix 를 맞출 것.
 * 에러를 던질 때는 {@code throw new BusinessException(ErrorCode.XXX)} 사용.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Common ───────────────────────────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

    // ── User / Auth ──────────────────────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U004", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "U005", "만료된 토큰입니다."),

    // ── Ledger ───────────────────────────────────────────────────────────────
    LEDGER_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "장부 항목을 찾을 수 없습니다."),
    /** 다른 user_no 의 장부에 접근 시도 — 반드시 403 으로 응답해야 함 (404 로 처리하면 존재 여부 노출). */
    LEDGER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "L002", "다른 사용자의 장부에 접근할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

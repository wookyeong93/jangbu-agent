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
 *
 * <p>HTTP 상태코드 규칙 (ADR-0006): 인증·인가(401/403)·서버에러(500)·요청 형식 오류(400)만
 * 실제 상태코드를 쓴다. 그 외 비즈니스 로직 결과(리소스 없음, 충돌, 로그인 시도 결과 등)는
 * 전부 {@code HttpStatus.OK}(200) + {@code success:false} 로 응답한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Common ───────────────────────────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.OK, "C004", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

    // ── User / Auth ──────────────────────────────────────────────────────────
    /** AuthService.login 전용 — 로그인 시도 자체의 결과(아이디 틀림). 인증된 세션 거부가 아님. */
    USER_NOT_FOUND(HttpStatus.OK, "U001", "사용자를 찾을 수 없습니다."),
    USER_ID_DUPLICATED(HttpStatus.OK, "U002", "이미 사용 중인 아이디입니다."),
    /** AuthService.login(로그인 결과) + UserService.updateProfile(현재 비밀번호 확인) 둘 다 비즈니스 결과. */
    INVALID_PASSWORD(HttpStatus.OK, "U003", "비밀번호가 올바르지 않습니다."),
    /** AuthService.reissue 전용 — refresh token 유효성. 진짜 인증 실패라 401 유지. */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U004", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "U005", "만료된 토큰입니다."),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.OK, "U006", "새 비밀번호는 현재 비밀번호와 달라야 합니다."),

    // ── Ledger ───────────────────────────────────────────────────────────────
    LEDGER_NOT_FOUND(HttpStatus.OK, "L001", "장부 항목을 찾을 수 없습니다."),
    /** 다른 user_no 의 장부에 접근 시도 — 인가 실패라 403 유지. */
    LEDGER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "L002", "다른 사용자의 장부에 접근할 수 없습니다."),
    /** 장부는 이미 발생한 거래만 기록한다 — 미래 날짜 등록 금지 (ADR-0007). */
    LEDGER_FUTURE_DATE_NOT_ALLOWED(HttpStatus.OK, "L003", "거래일은 오늘 이전 날짜만 등록할 수 있습니다."),

    // ── Dashboard ────────────────────────────────────────────────────────────
    DASHBOARD_PERIOD_TOO_LONG(HttpStatus.OK, "D001", "조회 기간은 최대 12개월까지 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

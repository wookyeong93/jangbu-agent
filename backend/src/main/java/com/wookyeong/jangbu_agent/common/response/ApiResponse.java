package com.wookyeong.jangbu_agent.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모든 API 응답을 감싸는 공통 래퍼.
 *
 * <p>성공 시:
 * <pre>{ "success": true,  "data": {...}, "error": null }</pre>
 * 실패 시:
 * <pre>{ "success": false, "data": null,  "error": {"code": "U001", "message": "..."} }</pre>
 *
 * <p>컨트롤러에서 직접 new 하지 말고 팩토리 메서드만 사용할 것:
 * <ul>
 *   <li>{@code ApiResponse.ok(data)}   — 데이터 반환
 *   <li>{@code ApiResponse.ok()}       — 본문 없는 성공 (204 스타일)
 *   <li>{@code ApiResponse.fail(code)} — 에러 반환 (GlobalExceptionHandler가 사용)
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * ErrorCode 에 정의된 메시지를 그대로 사용.
     * 커스텀 메시지가 필요하면 {@link #fail(ErrorCode, String)} 사용.
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, new ErrorDetail(errorCode.getCode(), errorCode.getMessage()));
    }

    /** 검증 오류처럼 에러 메시지를 동적으로 조합해야 할 때 사용. */
    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(errorCode.getCode(), message));
    }

    /** 에러 응답 페이로드. code 는 프론트 분기용, message 는 사용자 노출용. */
    @Getter
    @AllArgsConstructor
    public static class ErrorDetail {
        private final String code;
        private final String message;
    }
}

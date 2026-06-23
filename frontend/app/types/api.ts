/** 백엔드 ApiResponse<T> 래퍼와 1:1 대응 (ApiResponse.java). */
export interface ApiErrorDetail {
  code: string
  message: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiErrorDetail | null
}

/** success:false 응답을 던질 때 사용 — code로 분기, message는 사용자 노출용. */
export class ApiError extends Error {
  code: string

  constructor(detail: ApiErrorDetail) {
    super(detail.message)
    this.code = detail.code
  }
}

/** 서버가 응답 없이 끌고 가는 상황을 막기 위한 공통 요청 타임아웃(ms). */
export const API_TIMEOUT_MS = 3_000

export const NETWORK_ERROR_MESSAGE = '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.'

/**
 * $fetch 에러가 응답 자체를 못 받은 경우(네트워크 끊김·서버 다운·timeout)인지 판별.
 * ofetch는 이 경우 FetchError.response 가 없다 — 있으면 서버가 응답한 HTTP 에러(4xx/5xx).
 */
export function isNetworkError(err: unknown): boolean {
  return !(err && typeof err === 'object' && 'response' in err && (err as { response?: unknown }).response)
}

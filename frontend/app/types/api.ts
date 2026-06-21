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

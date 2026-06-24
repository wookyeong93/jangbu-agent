import { API_TIMEOUT_MS, isNetworkError, NETWORK_ERROR_MESSAGE } from '~/types/api'
import type { ApiResponse } from '~/types/api'
import type { LoginRequest, TokenResponse } from '~/types/auth'

/** AuthController 대응 (/api/auth/**). */

/**
 * useApiFetch를 거치지 않고 $fetch로 직접 처리한다 — 토큰이 아직 없는 호출이라
 * useApiFetch를 쓰면 401 → refresh → useApiFetch 를 다시 호출하는 순환이 생긴다.
 */
export async function login(body: LoginRequest): Promise<ApiResponse<TokenResponse>> {
  const config = useRuntimeConfig()
  try {
    return await $fetch<ApiResponse<TokenResponse>>('/api/auth/login', {
      method: 'POST',
      baseURL: config.public.apiBase,
      credentials: 'include',
      timeout: API_TIMEOUT_MS,
      body
    })
  } catch (err) {
    throw new Error(isNetworkError(err) ? NETWORK_ERROR_MESSAGE : '로그인에 실패했습니다.')
  }
}

/** 재발급 자체를 수행하는 호출이라 마찬가지로 $fetch로 직접 처리한다. */
export function refresh(): Promise<ApiResponse<TokenResponse>> {
  const config = useRuntimeConfig()
  return $fetch<ApiResponse<TokenResponse>>('/api/auth/refresh', {
    method: 'POST',
    baseURL: config.public.apiBase,
    credentials: 'include',
    timeout: API_TIMEOUT_MS
  })
}

export function logout(): Promise<void> {
  return useApiFetch<void>('/api/auth/logout', { method: 'POST' })
}

import { defineStore } from 'pinia'
import { API_TIMEOUT_MS, isNetworkError, NETWORK_ERROR_MESSAGE } from '~/types/api'
import type { ApiResponse } from '~/types/api'
import type { LoginRequest, TokenResponse, UserResponse } from '~/types/auth'

/**
 * accessToken은 메모리에만 보관 (새로고침하면 사라짐 — refresh token httpOnly 쿠키로
 * 재발급받아 복구하는 흐름은 middleware/auth.global.ts 에서 라우트 진입마다 처리한다).
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const profile = ref<UserResponse | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(userId: string, password: string) {
    const config = useRuntimeConfig()
    const body: LoginRequest = { userId, password }
    let res: ApiResponse<TokenResponse>
    try {
      res = await $fetch<ApiResponse<TokenResponse>>('/api/auth/login', {
        method: 'POST',
        baseURL: config.public.apiBase,
        credentials: 'include',
        timeout: API_TIMEOUT_MS,
        body
      })
    } catch (err) {
      throw new Error(isNetworkError(err) ? NETWORK_ERROR_MESSAGE : '로그인에 실패했습니다.')
    }

    if (!res.success || !res.data) {
      throw new Error(res.error?.message ?? '로그인에 실패했습니다.')
    }

    accessToken.value = res.data.accessToken
    await fetchProfile()
  }

  async function fetchProfile() {
    profile.value = await useApiFetch<UserResponse>('/api/users/me')
  }

  /**
   * Access Token 만료 시 1회 시도. Refresh Token도 만료/무효면 세션 전체 만료로 보고
   * 상태를 초기화한다. 로그인 페이지로의 리다이렉트는 호출부(middleware/useApiFetch)에서
   * 반환값(false)을 보고 처리한다.
   */
  async function refreshAccessToken(): Promise<boolean> {
    try {
      const config = useRuntimeConfig()
      const res = await $fetch<ApiResponse<TokenResponse>>('/api/auth/refresh', {
        method: 'POST',
        baseURL: config.public.apiBase,
        credentials: 'include',
        timeout: API_TIMEOUT_MS
      })
      if (res.success && res.data) {
        accessToken.value = res.data.accessToken
        return true
      }
    } catch {
      // refresh token도 만료/무효 — 아래에서 세션 초기화
    }
    clear()
    return false
  }

  async function logout() {
    try {
      await useApiFetch('/api/auth/logout', { method: 'POST' })
    } finally {
      clear()
    }
  }

  function clear() {
    accessToken.value = null
    profile.value = null
  }

  return { accessToken, profile, isAuthenticated, login, fetchProfile, refreshAccessToken, logout, clear }
})

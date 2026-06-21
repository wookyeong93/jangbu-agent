import { defineStore } from 'pinia'
import type { ApiResponse } from '~/types/api'
import type { TokenResponse, UserResponse } from '~/types/auth'

/**
 * accessToken은 메모리에만 보관 (새로고침하면 사라짐 — refresh token httpOnly 쿠키로
 * 재발급받아 복구하는 흐름은 앱 진입 시점 초기화 로직에서 처리할 것, 아직 미구현).
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const profile = ref<UserResponse | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(userId: string, password: string) {
    const config = useRuntimeConfig()
    const res = await $fetch<ApiResponse<TokenResponse>>('/api/auth/login', {
      method: 'POST',
      baseURL: config.public.apiBase,
      credentials: 'include',
      body: { userId, password }
    })

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
   * 상태를 초기화한다 (로그인 페이지로의 리다이렉트는 아직 미구현 — 로그인 폼 작업에서 추가).
   */
  async function refreshAccessToken(): Promise<boolean> {
    try {
      const config = useRuntimeConfig()
      const res = await $fetch<ApiResponse<TokenResponse>>('/api/auth/refresh', {
        method: 'POST',
        baseURL: config.public.apiBase,
        credentials: 'include'
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

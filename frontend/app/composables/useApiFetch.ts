import type { ApiResponse } from '~/types/api'
import { API_TIMEOUT_MS, ApiError, NETWORK_ERROR_MESSAGE, isNetworkError } from '~/types/api'

interface UseApiFetchOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  // ofetch의 body 타입(Record<string, any>)과 맞춰야 LoginRequest 같은 구체 DTO 인터페이스를
  // 그대로 넘길 수 있다 — Record<string, unknown>은 인덱스 시그니처가 없는 인터페이스를 거부한다.
  body?: Record<string, any>
  query?: Record<string, unknown>
}

/**
 * 인증 헤더 자동 첨부 + 401 시 1회 자동 재발급(refresh)·재시도 + ApiResponse 언래핑.
 *
 * <p>컴포넌트·스토어에서 백엔드를 호출할 때 직접 $fetch 를 쓰지 말고 항상 이 함수를
 * 거친다 (frontend/CLAUDE.md). 단, 로그인/회원가입/재발급처럼 토큰이 아직 없거나
 * 재발급 자체를 수행하는 호출은 stores/auth.ts 에서 $fetch 로 직접 처리한다
 * (이 함수가 다시 401 → refresh → 이 함수를 호출하는 순환을 피하기 위함).
 *
 * <p>401 은 Spring Security가 직접 막는 경로지만, RestAuthenticationEntryPoint(백엔드)가
 * ApiResponse 포맷으로 응답하며 만료(U005)와 그 외 무효 사유(C002 등)를 error.code로 구분해
 * 내려준다 — 그래서 상태코드만이 아니라 error.code === 'U005' 일 때만 재발급을 시도한다.
 */
export async function useApiFetch<T>(path: string, options: UseApiFetchOptions = {}): Promise<T> {
  const config = useRuntimeConfig()
  const auth = useAuthStore()

  async function call(): Promise<T> {
    const res = await $fetch<ApiResponse<T>>(path, {
      baseURL: config.public.apiBase,
      credentials: 'include',
      timeout: API_TIMEOUT_MS,
      method: options.method ?? 'GET',
      body: options.body,
      query: options.query,
      headers: auth.accessToken ? { Authorization: `Bearer ${auth.accessToken}` } : undefined
    })

    if (!res.success) {
      throw new ApiError(res.error ?? { code: 'C500', message: '알 수 없는 오류가 발생했습니다.' })
    }
    return res.data as T
  }

  try {
    return await call()
  } catch (err: any) {
    // call() 안에서 이미 던진 ApiError(success:false, 비즈니스 로직 실패)는 그대로 전달.
    if (err instanceof ApiError) {
      throw err
    }

    // 응답 자체를 못 받은 경우(네트워크 끊김·서버 다운·timeout) — 재발급 시도 의미 없이 바로 안내.
    if (isNetworkError(err)) {
      throw new ApiError({ code: 'NETWORK_ERROR', message: NETWORK_ERROR_MESSAGE })
    }

    const status = err?.response?.status
    const code = err?.data?.error?.code

    // 액세스 토큰 만료(U005)일 때만 재발급 시도. 그 외 401/403은 재시도해도 안 풀리므로 그대로 던짐.
    if (status === 401 && code === 'U005') {
      const refreshed = await auth.refreshAccessToken()
      if (refreshed) {
        return await call()
      }
      // refresh token도 만료/무효 — 세션 전체 만료로 보고 로그인 페이지로 보낸다.
      await navigateTo('/login')
    }
    throw err
  }
}

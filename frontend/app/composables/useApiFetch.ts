import type { ApiResponse } from '~/types/api'
import { ApiError } from '~/types/api'

interface UseApiFetchOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
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
 * <p>401 은 Spring Security가 직접 막는 경로라 ApiResponse 포맷이 아닐 수 있다
 * (ADR-0006 알려진 갭) — 그래서 상태코드만으로 재발급 트리거를 판단한다.
 */
export async function useApiFetch<T>(path: string, options: UseApiFetchOptions = {}): Promise<T> {
  const config = useRuntimeConfig()
  const auth = useAuthStore()

  async function call(): Promise<T> {
    const res = await $fetch<ApiResponse<T>>(path, {
      baseURL: config.public.apiBase,
      credentials: 'include',
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
    const status = err?.response?.status

    // 액세스 토큰 만료로 보이는 401 만 재발급 시도. 403(권한 없음 등)은 재시도해도 안 풀리므로 그대로 던짐.
    if (status === 401 && auth.accessToken) {
      const refreshed = await auth.refreshAccessToken()
      if (refreshed) {
        return await call()
      }
    }
    throw err
  }
}

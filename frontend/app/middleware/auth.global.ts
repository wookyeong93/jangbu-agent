/**
 * 모든 라우트 진입 시 인증 상태를 확인한다.
 *
 * <p>accessToken은 메모리(Pinia)에만 있어 새로고침하면 사라진다 — 이때 메모리에 토큰이
 * 없다고 바로 로그인으로 보내지 않고 refresh token(httpOnly 쿠키)으로 먼저 재발급을
 * 시도해 세션을 복구한다. 그래도 실패하면(쿠키도 만료/없음) 로그인 페이지로 보낸다.
 *
 * <p>탭을 보고 있다가 한동안 자리를 비워 access token이 만료된 경우는 여기서 잡지 않고,
 * 해당 페이지가 API를 호출할 때 useApiFetch 의 401(U005) → refresh 흐름에서 처리한다.
 *
 * <p>profile은 login() 안에서만 채워진다 — 새로고침 복구 경로(refreshAccessToken)는
 * accessToken만 갱신하므로, AppHeader 등에서 쓸 profile이 비어 있으면 여기서 같이 채운다.
 */
export default defineNuxtRouteMiddleware(async (to) => {
  if (to.path === '/login') {
    return
  }

  const auth = useAuthStore()
  if (!auth.isAuthenticated) {
    const refreshed = await auth.refreshAccessToken()
    if (!refreshed) {
      return navigateTo('/login')
    }
  }

  if (!auth.profile) {
    await auth.fetchProfile()
  }
})

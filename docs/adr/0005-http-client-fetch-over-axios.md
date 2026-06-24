# ADR-0005: HTTP 클라이언트로 axios 대신 Nuxt 내장 $fetch/useFetch 사용

## 맥락
- 프론트엔드(Nuxt 4)에서 백엔드 API를 호출할 HTTP 클라이언트를 정해야 함.
- axios는 가장 널리 쓰이는 선택지지만, 2026-03-31 npm 계정 탈취로 axios@1.14.1·0.30.4에
  악성 의존성(plain-crypto-js, crypto-js 타이포스쿼팅)이 주입되어 RAT가 배포된 공급망
  공격이 있었음 (북한 연계 행위자로 추정, Microsoft/Google 위협 인텔리전스 보고).
- Nuxt는 ofetch 기반의 `$fetch`/`useFetch`를 기본 내장하고 있어 별도 설치 없이 동일 기능 가능.

## 결정
- axios를 의존성에 추가하지 않고, Nuxt 내장 `$fetch`/`useFetch`만 사용한다.
- 인증 헤더 첨부 등 공통 로직은 `composables/useApiFetch.ts`에서 한 곳으로 모은다.

## 고려한 대안
1. **axios (안전 버전 1.15.1+ 고정)**
   - interceptor 기반 공통 처리에 익숙하고 생태계가 넓음
   - 그러나 외부 의존성이 하나 늘고, 최근 사례처럼 유지보수자 계정 탈취형 공급망 공격에
     노출되는 표면이 늘어남. 토이 프로젝트에서 감수할 이유가 약함.
2. **ky 등 경량 fetch 래퍼**
   - 가볍지만 역시 외부 의존성 추가. Nuxt 내장 기능으로 충분히 대체 가능.
3. **$fetch/useFetch (채택)**
   - 외부 HTTP 클라이언트 의존성 0개. Nuxt SSR/유니버설 렌더링과 자동 통합
     (`useFetch`가 서버·클라이언트 중복 호출 방지).

## 결과
- (+) HTTP 클라이언트발 공급망 공격 표면 제거
- (+) Nuxt 렌더링 모델과 네이티브 통합, 추가 학습 비용 없음
- (-) axios의 interceptor만큼 편리한 추상화가 기본 제공되지 않음 — 인증 헤더·에러 처리를
      `useApiFetch.ts`에서 직접 구현해야 함
- (-) 향후 axios 전용 기능(특정 어댑터, 업로드 진행률 등)이 꼭 필요해지면 재검토

# Frontend 규칙

## 스택
- Vue 3 (Composition API만 사용, Options API 금지) + Nuxt 4 + TypeScript
- 상태관리: Pinia (`defineStore`도 setup 스타일로 작성)
- 스타일: Tailwind CSS, 라이트 테마(관리자 백오피스 톤) — 색상 토큰은 `app/assets/css/main.css`의 CSS 변수로 관리
- 차트: Chart.js + vue-chartjs
- HTTP 통신: Nuxt 내장 `$fetch`/`useFetch`(ofetch 기반)만 사용. axios 사용 금지 — [ADR-0005](../docs/adr/0005-http-client-fetch-over-axios.md)

## 작업 방식
- 루트 CLAUDE.md 규칙 그대로 적용: 변경 전 계획 제시 → 승인 → 구현, 한 단계 = 한 검토 = 한 커밋.
- "다음 단계가 자연스러운 연장"이라는 이유로 합의된 범위 넘어가서 파일 만들지 말 것. 범위가 애매하면 진행 전에 먼저 물을 것.

## 폴더 구조 (`app/` 디렉토리 기준, Nuxt 4)
```
app/
├── assets/css/      — 전역 스타일, Tailwind 진입점
├── components/      — 재사용 컴포넌트 (PascalCase 파일명)
├── composables/     — use* 함수 (API 호출, 인증 등 횡단 로직)
├── constants/       — 네비게이션 메뉴 등 정적 상수
├── layouts/         — default(사이드바+헤더), auth(로그인 등 단독 페이지)
├── pages/           — 라우트 단위 페이지
└── stores/          — Pinia 스토어
```

## 컴포넌트 컨벤션
- 모든 컴포넌트는 `<script setup lang="ts">` + Composition API.
- 정적 데이터(메뉴 목록 등)는 컴포넌트 안에 인라인하지 말고 `constants/`로 분리.
- 백엔드 API 응답 타입은 `types/`에 정의하고 컴포넌트·스토어에서 재사용 (백엔드 DTO 구조와 1:1 대응 유지).

## API 통신
- `runtimeConfig.public.apiBase`로 백엔드 URL 주입 (`NUXT_PUBLIC_API_BASE` 환경변수, 기본 `http://localhost:8080`).
- 인증 헤더 첨부·에러 처리는 `composables/useApiFetch.ts` 한 곳에서만 처리. 컴포넌트에서 직접 `$fetch` 호출 금지.

## 인증
- accessToken은 Pinia 스토어(`stores/auth.ts`)에서 메모리로 관리. Refresh Token은 백엔드가 httpOnly 쿠키로 관리하므로 프론트에서 직접 다루지 않음.

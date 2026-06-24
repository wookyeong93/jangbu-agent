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
├── api/             — 백엔드 호출 함수. 백엔드 컨트롤러 1개당 파일 1개 (예: AuthController → api/auth.ts)
├── assets/css/      — 전역 스타일, Tailwind 진입점
├── components/      — 재사용 컴포넌트 (PascalCase 파일명)
├── composables/     — use* 함수 (인증 등 횡단 로직). API 호출 자체는 api/로, 여긴 그걸 감싸는 로직만.
├── constants/       — 네비게이션 메뉴 등 정적 상수
├── layouts/         — default(사이드바+헤더), auth(로그인 등 단독 페이지)
├── pages/           — 라우트 단위 페이지
└── stores/          — Pinia 스토어
```

## 화면 / API 분리
- 화면(`pages/`, `components/`)은 화면만 — API 호출을 직접 하지 않고 `api/`의 함수를 불러서 쓴다.
- API 호출 함수는 전부 `api/`에 둔다. 백엔드 컨트롤러와 1:1로 파일을 나눈다
  (예: `AuthController` → `api/auth.ts`, `UserController` → `api/user.ts`, `DashboardController` → `api/dashboard.ts`).
- `stores/`는 상태(토큰, 프로필 등)를 들고 있는 경우에만 존재 — 그 안에서 `api/`의 함수를 불러 상태를 갱신하는
  흐름 제어를 담당한다. 상태가 없는 단순 조회는 스토어 없이 화면이 `api/` 함수를 바로 쓴다 (예: 대시보드).
- `composables/`는 `use*` 형태의 횡단 로직(`useApiFetch` 같은 공통 HTTP 래퍼)만 — 특정 백엔드 엔드포인트를
  호출하는 코드를 넣지 않는다.

## API 통신
- `runtimeConfig.public.apiBase`로 백엔드 URL 주입 (`NUXT_PUBLIC_API_BASE` 환경변수, 기본 `http://localhost:8080`).
- 인증 헤더 첨부·에러 처리는 `composables/useApiFetch.ts` 한 곳에서만 처리. `api/` 밖에서 직접 `$fetch`/`useApiFetch` 호출 금지.
- API 함수의 파라미터는 위치 인자가 아니라 객체로 받는다 (예: `getDashboard({ months: 6 })`, `getDashboard(6)` 금지).
- 백엔드가 이미 기본값을 가진 파라미터는 프론트에서 재차 하드코딩하지 않는다 — 값을 안 넘기면 query
  자체를 생략해 백엔드 기본값을 그대로 따른다.

## 컴포넌트 컨벤션
- 모든 컴포넌트는 `<script setup lang="ts">` + Composition API.
- 정적 데이터(메뉴 목록 등)는 컴포넌트 안에 인라인하지 말고 `constants/`로 분리.
- 백엔드 API 요청/응답 타입은 `types/`에 정의하고 `api/`·컴포넌트·스토어에서 재사용 (백엔드 DTO 구조와 1:1 대응 유지).
- 재사용되는 UI 패턴(버튼, 입력창, 셀렉트 등)은 페이지마다 Tailwind 클래스를 반복해서 박아넣지 말고
  `components/App*.vue` 공통 컴포넌트로 추출한다. 새 화면 작업 전에 비슷한 패턴이 이미 있는지 먼저 확인.
- 모달(오버레이)은 배경 클릭으로 닫히지 않는다. ESC 키 또는 명시적 취소/확인 버튼으로만 닫는다.

## 데일리 가이드 화면
- AI 데일리 가이드(`GET /api/guide/daily`)는 별도 메뉴·페이지가 아니라 메인 대시보드에 카드 형태로
  노출한다. 매입 입력 시점에 말풍선으로 띄우는 방식은 검토 후 폐기된 안이다.

## 인증
- accessToken은 Pinia 스토어(`stores/auth.ts`)에서 메모리로 관리. Refresh Token은 백엔드가 httpOnly 쿠키로 관리하므로 프론트에서 직접 다루지 않음.

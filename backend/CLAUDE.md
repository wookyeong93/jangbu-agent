# Backend 규칙

## 스택
- Java 25, Spring Boot 4, PostgreSQL
- 인증: Spring Security + JWT (user_no 기준 장부 격리)
- LLM: Gemini API (해석/조언 생성에만, 무료 티어 — ADR-0004)

## 영속성 — 이 분기 규칙을 반드시 지킬 것
- 단순 CRUD, 타입세이프 동적 쿼리 → JPA + QueryDSL
- 복잡한 통계/집계 쿼리(윈도우 함수 등) → MyBatis
- 통계 쿼리를 JPA로 억지로 짜지 말 것. 위 기준으로 분리.

## 도메인 규칙
- 모든 장부 조회는 user_no로 격리된다. 다른 사용자 데이터 접근 금지.
- 가이드 생성 시 계산에 쓴 기준 수치(매입/매출/지출/순익)를 tb_daily_guide에 스냅샷으로 저장.

## 장부 도메인
> 상세 정책: [docs/policy/ledger.md](../docs/policy/ledger.md) | 삭제 결정: [docs/adr/0003-user-delete-ledger.md](../docs/adr/0003-user-delete-ledger.md)

### 등록·수정 규칙
- `trx_type`: `PURCHASE`(매입) / `SALE`(매출) / `EXPENSE`(지출) 3종만 허용. db_dump.sql 참조. 그 외 값은 서비스 레이어에서 거부.
- `amount`: 0 이상 정수만 허용. 소수점 불가.
- `trx_date`: 기본값 오늘. 과거 날짜로는 수정 가능하나 미래 날짜는 허용하지 않는다 (ADR-0007) —
  장부는 이미 발생한 거래만 기록한다.
- `trx_name`: 선택 입력 (NULL 허용).
- 수정 가능 필드: `trx_type` / `trx_date` / `amount` / `trx_name`. `user_no`·`ledger_no`는 변경 불가.

### 삭제 정책
- user 삭제 시 해당 user의 장부 전체를 물리 삭제 (`ON DELETE CASCADE`). 거래기록 보존 안 함 (ADR-0003).

### 월별 KPI (파생값 — DB 미저장, 조회 시 계산)
- 총매입 / 총매출 / 총지출: 해당 월 `trx_type`별 `amount` 합산
- 순이익 = 매출 − 매입 − 지출
- 마진율(%) = (매출 − 매입) / 매입 × 100. 매입 = 0이면 마진율 = 0 (zero-division 방지)

## 패키지 구조

```
com.wookyeong.jangbu_agent
├── common/                  — 공통 인프라 (횡단 관심사)
│   ├── config/              — Spring 설정 빈 (JPA 등)
│   ├── entity/              — BaseEntity (공통 감사 필드: created_at, updated_at 등)
│   ├── exception/           — BusinessException, GlobalExceptionHandler
│   └── response/            — ApiResponse<T>, ErrorCode
├── domain/                  — 비즈니스 도메인 (도메인별 수직 분리)
│   ├── code/                — 공통 코드 (tb_group_code, tb_code)
│   ├── guide/               — 데일리 가이드 (tb_daily_guide, AI 조언 결과)
│   ├── ledger/              — 원장 (tb_ledger, 매입·판매·지출 거래)
│   └── user/                — 사용자 (tb_user, 인증 주체)
│       ├── controller/      — REST 컨트롤러
│       ├── dto/             — 요청·응답 DTO
│       ├── entity/          — JPA 엔티티
│       ├── repository/      — JPA Repository / QueryDSL
│       └── service/         — 비즈니스 로직
└── infra/                   — 인프라 관심사 (Spring Security, JWT, 외부 API)
    ├── logging/             — MDC 필터 (RequestLoggingFilter)
    └── security/
        ├── SecurityConfig.java
        └── jwt/             — JWT 발급·검증·필터
```

- 모든 도메인은 `controller / dto / entity / repository / service` 5-레이어 구조를 따른다.
- `common`과 `infra`는 도메인에 의존하지 않는다. 도메인이 `common`을 사용한다.
- 도메인 간 직접 의존은 금지. 공유 데이터가 필요하면 `common` 경유 또는 ID 참조만 허용.

## API 응답 — HTTP 상태코드 컨벤션
> 상세 배경: [ADR-0006](../docs/adr/0006-http-status-convention.md)

- **인증·인가 실패(401/403)**, **서버·인프라 오류(500)** 만 실제 HTTP 상태코드로 응답한다.
- 그 외 모든 비즈니스 로직 결과(리소스 없음, 중복 충돌, 로그인 시도 결과 등)는 HTTP `200` +
  `ApiResponse{ success: false, error: { code, message } }` 로 응답한다. 새 `ErrorCode`를 추가할 때
  이 기준으로 `HttpStatus`를 정할 것 — 임의로 404/409 등을 쓰지 않는다.
- "요청 형식 자체가 잘못됨"(Bean Validation 실패, 타입 불일치)은 `400`을 유지한다 — 인증/인가/서버
  오류는 아니지만 클라이언트가 보낸 요청 자체가 처리 불가능한 형태였다는 의미라 예외로 둔다.
- JWT는 유효하지만 참조하는 사용자가 그 사이 삭제된 경우(FK 참조 실패)는 `existsById`로 사전
  확인해 `ErrorCode.FORBIDDEN`(403)을 명시적으로 던질 것. `getReferenceById`의 지연 로딩에 맡겨
  의도치 않은 500이 나가지 않게 한다.
- Spring Security가 `DispatcherServlet` 진입 전에 직접 막는 401(미인증)은 `GlobalExceptionHandler`를
  거치지 않아 `ApiResponse` 포맷이 아니다 — 현재 의도된 예외이며 통일 대상이 아니다.

## Observability

### 목표
서버 모니터링 시 에러 로그 추적. 비즈니스 예외(도메인 규칙 위반)와 서버 예외를 로그 레벨로 구분한다.

### MDC 필드
`RequestLoggingFilter`(OncePerRequestFilter)가 모든 요청 진입 시 자동 주입하고, 응답 후 반드시 제거한다.
- `requestId`: UUID — 동일 요청의 로그를 하나로 묶는 트레이싱 키
- `userId`: JWT에서 파싱한 userId. 미인증 요청(회원가입·로그인)은 `"anonymous"`

### 로그 레벨 기준
| 레벨  | 대상                                              |
|-------|---------------------------------------------------|
| WARN  | BusinessException, MethodArgumentNotValidException |
| ERROR | 그 외 모든 예외 (서버 오류)                         |

- 정상 흐름에는 INFO 이하 로그를 찍지 않는다 (노이즈 차단).
  - 예외: LLM 외부 호출(Gemini 등)은 비용·성능 모니터링 목적으로 호출 1건당 INFO 로그 1줄
    (모델명, 소요시간ms, 토큰 사용량)을 남긴다. 그 외 도메인 정상 흐름 로그는 계속 금지.
- 스택 트레이스는 ERROR 레벨에만 포함한다.
- 인증 실패(401, 토큰 없음·위조·만료)는 `RequestLoggingFilter`보다 먼저 실행되는
  `RestAuthenticationEntryPoint`에서 WARN으로 남긴다. 이 시점은 MDC 주입 전이라 requestId·userId가
  비어있게 찍힌다 — 보안 필터 체인이 로깅 필터보다 먼저 도는 구조상 제약이며, 로그 메시지에 요청
  URI를 포함해 최소한의 추적 정보를 남긴다.

### 구현 위치
- `infra/logging/RequestLoggingFilter` — MDC requestId·userId 주입 및 요청 완료 후 정리
- `common/exception/GlobalExceptionHandler` — 예외 레벨 분리 (WARN / ERROR)
- `infra/ai/GeminiClient` — LLM 호출 1건당 INFO 로그 (모델·소요시간·토큰 사용량)
- `infra/security/RestAuthenticationEntryPoint` — 인증 실패 WARN 로그

## 주석 컨벤션
- `common/`, `infra/` 파일에는 클래스·메서드 Javadoc을 작성한다 — 진입점 코드(ApiResponse 팩토리,
  ErrorCode 체계, SecurityConfig 등)는 주석 없이는 사용법을 파악하기 어렵기 때문.
- `domain/` 비즈니스 로직은 잘 지어진 이름으로 의도가 드러나면 불필요한 주석을 추가하지 않는다.

## 테스트
- 집계·계산 로직은 단위 테스트 필수. 경계값(0건, 단일건, 월 경계) 포함.
- LLM 응답 품질 Eval은 일반 단위 테스트와 분리한다. `@Tag("eval")`을 붙이고 `./gradlew evalTest`로만
  실행 (기본 `test`는 `excludeTags 'eval'`). 실제 모델을 호출하므로 `GEMINI_API_KEY` 없으면 자동 스킵
  (`@EnabledIfEnvironmentVariable`). 새 LLM 기능을 추가하면 `GuideEvalTest`처럼 대표 시나리오별로
  응답이 비어있지 않은지 + Guardrail을 통과하는지 검증하는 케이스를 함께 만든다.

## 사용자 도메인
> 상세 정책: [docs/policy/user.md](../docs/policy/user.md)

- `userId`(아이디)는 불변. 수정 불가.
- 수정 가능 필드: `userNm`(이름), `userPwd`(비밀번호). 둘 다 미전달 시 400.
- 비밀번호 변경 시 `currentPassword` 필수 → 불일치 시 401, 새 비밀번호 = 현재 비밀번호 시 400.

## 인증·인가
> 상세 정책: [docs/policy/auth.md](../docs/policy/auth.md)

- JWT 검증은 Security 필터에서 진입 시 1회. `user_no`를 `SecurityContext`에 확립.
- 모든 장부 조회·집계는 인증된 `user_no`로 격리. 다른 사용자 데이터 접근 금지.
- JWT 구현체는 `infra/security/jwt/` 패키지에만 위치한다. 도메인 레이어에서 토큰 직접 파싱 금지.

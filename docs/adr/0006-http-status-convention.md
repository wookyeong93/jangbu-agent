# ADR-0006: HTTP 상태코드 컨벤션 — 인증·인가·서버에러만 실제 코드, 나머지는 200+메시지

## 맥락
- 기존 `ErrorCode`는 `NOT_FOUND`(404), `USER_ID_DUPLICATED`(409), `SAME_AS_CURRENT_PASSWORD`(400) 등
  도메인 결과별로 다양한 HTTP 상태를 사용했음. 프론트엔드에서 분기가 늘어나고 일관성이 떨어짐.
- 전체 백엔드 스캔 결과 추가로 확인된 사항:
  - Spring Security가 미인증 요청을 막는 경로는 `DispatcherServlet` 진입 전이라
    `GlobalExceptionHandler`(`@RestControllerAdvice`)를 거치지 않음. 게다가 별도
    `AuthenticationEntryPoint`를 지정하지 않으면 Spring Security 기본값
    (`Http403ForbiddenEntryPoint`)이 미인증 요청도 403으로 응답해버려 — 토큰이 전혀
    없거나 위조된 요청도 403이 나가고 있었음 (코드 주석은 401을 가정했지만 실제로는
    한 번도 401을 낸 적이 없었음). "재인증하면 통과될 수 있다(401)"와 "유효한 인증으로도
    거부된다(403)"의 의미가 섞여 프론트의 401-재발급 컨벤션이 동작할 수 없는 상태였음.
    → `RestAuthenticationEntryPoint` 추가로 해결 (아래 결정 참고). 이제 미인증 요청은
    401 + `ApiResponse` 포맷으로 응답한다 — 포맷 통일까지 같이 해결됨.
  - 의도치 않게 500으로 떨어지는 케이스: `GeminiClient`(Gemini 응답 비어있음),
    `JwtTokenProvider.hashToken`(SHA-256 알고리즘 사용 불가 — 환경 문제, 500 유지가 맞음),
    `LedgerService`/`GuideService`/`UserService`의 FK 참조(JWT는 유효하지만 그 사이 계정이
    삭제되어 참조가 실패하는 경우).
  - `ErrorCode.USER_NOT_FOUND`가 의미가 다른 두 곳에 쓰이고 있었음: ① `AuthService.login`의
    "로그인 시도 자체의 결과(아이디 틀림)" ② `UserService.updateProfile`의 "JWT는 유효한데
    그 사이 계정이 삭제된 경우". 후자는 ①과 분리해 FK 참조 실패 케이스로 합친다.

## 결정
- **요청 형식 자체가 잘못됨** (Bean Validation 실패, 타입 불일치, 허용값 외 입력) → 그대로 `400`.
- **인증·인가 실패** (401/403) → 그대로 유지. 단, "로그인 시도 자체의 결과"(아이디·비밀번호 틀림)는
  여기서 제외하고 200으로 분류한다 — 이미 인증된 세션에 대한 거부가 아니라 로그인 요청 처리의
  비즈니스 결과이기 때문.
- **JWT는 유효하지만 참조하는 사용자가 더 이상 존재하지 않는 경우** (FK 참조 실패) → `existsById`로
  사전 확인해 `ErrorCode.FORBIDDEN`(403)을 명시적으로 던진다. 적용 대상: `LedgerService.create`,
  `GuideService.getOrCreateDailyGuide`, `UserService.updateProfile`(기존 `USER_NOT_FOUND` 대체).
- **미인증 요청** (토큰 없음·위조·만료) → `RestAuthenticationEntryPoint`가 401 +
  `ApiResponse` 포맷으로 명시 응답 (Spring Security 기본값인 403 폴백 대신).
- **서버·인프라 오류** (500) → 그대로 유지.
- **그 외 모든 비즈니스 로직 결과** (리소스 없음, 중복 충돌, 로그인 시도 결과, 비밀번호 확인 결과 등
  "요청은 정상인데 결과가 부정적인 경우") → HTTP `200` + `{ success: false, error: { code, message } }`.

### ErrorCode 재분류

| ErrorCode | 사용처 | 변경 전 | 변경 후 |
|---|---|---|---|
| INVALID_INPUT | 요청 형식 오류 전반 | 400 | 400 (유지) |
| UNAUTHORIZED | (미사용, 제네릭) | 401 | 401 (유지) |
| FORBIDDEN | LEDGER_ACCESS_DENIED류 + FK 참조 실패(신규 적용) | 403 | 403 (유지) |
| NOT_FOUND | (미사용, 제네릭) | 404 | **200** |
| INTERNAL_SERVER_ERROR | 그 외 미처리 예외 | 500 | 500 (유지) |
| USER_NOT_FOUND | `AuthService.login` (아이디 틀림) 전용으로 한정 | 404 | **200** |
| USER_ID_DUPLICATED | `AuthService.signup` | 409 | **200** |
| INVALID_PASSWORD | `AuthService.login` + `UserService.updateProfile`(현재 비밀번호 확인) | 401 | **200** |
| INVALID_TOKEN | `AuthService.reissue` (refresh token 유효성) | 401 | 401 (유지) |
| EXPIRED_TOKEN | (미사용) | 401 | 401 (유지, 향후 사용 대비) |
| SAME_AS_CURRENT_PASSWORD | `UserService.updateProfile` | 400 | **200** |
| LEDGER_NOT_FOUND | `LedgerService` | 404 | **200** |
| LEDGER_ACCESS_DENIED | `LedgerService` (타 사용자 장부 접근) | 403 | 403 (유지) |

`MethodArgumentNotValidException`/`ConstraintViolationException`/`MethodArgumentTypeMismatchException`
핸들러(전부 `INVALID_INPUT` 계열 — 요청 형식 문제)는 `400` 유지.

## 고려한 대안
1. **모든 에러를 실제 HTTP 상태로 (기존 방식, 기각)** — REST 관행에는 맞지만 프론트 분기가 상태코드와
   메시지 두 군데로 흩어짐.
2. **전부 200+메시지로 (기각)** — 인증/인가/서버에러까지 200으로 가리면 모니터링·리버스 프록시·
   브라우저 fetch 에러 핸들링이 실제 장애를 못 잡음.
3. **인증/인가/서버에러만 상태코드, 나머지는 200+메시지 (채택)** — "이 요청은 시스템적으로
   실패했는가(상태코드)" 와 "비즈니스적으로 원하는 결과가 아닌가(메시지)"를 분리.

## 결과
- (+) 비즈니스 로직 응답이 `200 + success:false` 하나의 패턴으로 단순화, 프론트 분기 단순
- (+) 실제 장애(401/403/500)는 여전히 상태코드로 구분 가능 — 모니터링·프록시 영향 없음
- (+) FK 참조 실패가 의도치 않은 500이 아니라 명시적 403으로 정리됨, `USER_NOT_FOUND`의
      의미 혼용도 해소됨
- (+) 미인증 요청이 실제로 401을 내려보내게 됨 (이전엔 항상 403) — 프론트의
      "401이면 토큰 재발급 시도" 컨벤션이 정상 동작, 응답 포맷도 `ApiResponse`로 통일됨
- (-) REST 표준 관행(404/409 등)과 다름 — 외부 공개 API라면 부적합할 수 있음. 내부용 토이
      프로젝트 범위에서는 허용
- (-) 기존 `LEDGER_NOT_FOUND` 등을 기대하던 테스트·클라이언트 코드 전부 수정 필요

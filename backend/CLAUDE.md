# Backend 규칙

## 스택
- Java 25, Spring Boot 4, PostgreSQL
- 인증: Spring Security + JWT (user_no 기준 장부 격리)
- LLM: OpenAI API (해석/조언 생성에만)

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
- `trx_type`: `매입` / `매출` / `지출` 3종만 허용. 그 외 값은 서비스 레이어에서 거부.
- `amount`: 양수 정수만 허용. 소수점 불가. 0 불가.
- `trx_date`: 기본값 오늘. 사용자가 과거·미래 날짜로 수정 가능.
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
    └── security/
        ├── SecurityConfig.java
        └── jwt/             — JWT 발급·검증·필터
```

- 모든 도메인은 `controller / dto / entity / repository / service` 5-레이어 구조를 따른다.
- `common`과 `infra`는 도메인에 의존하지 않는다. 도메인이 `common`을 사용한다.
- 도메인 간 직접 의존은 금지. 공유 데이터가 필요하면 `common` 경유 또는 ID 참조만 허용.

## 테스트
- 집계·계산 로직은 단위 테스트 필수. 경계값(0건, 단일건, 월 경계) 포함.

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

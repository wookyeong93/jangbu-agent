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
- 순익 = 판매합 − 매입합 − 지출합. amount는 양수 절댓값, trx_type으로 구분.
- 가이드 생성 시 계산에 쓴 기준 수치(매입/매출/지출/순익)를 tb_daily_guide에 스냅샷으로 저장.

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

## 인증·인가
> 상세 정책: [docs/policy/auth.md](../docs/policy/auth.md)

- JWT 검증은 Security 필터에서 진입 시 1회. `user_no`를 `SecurityContext`에 확립.
- 모든 장부 조회·집계는 인증된 `user_no`로 격리. 다른 사용자 데이터 접근 금지.
- JWT 구현체는 `infra/security/jwt/` 패키지에만 위치한다. 도메인 레이어에서 토큰 직접 파싱 금지.

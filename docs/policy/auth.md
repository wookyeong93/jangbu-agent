# 인증·인가 정책

> 연관 ADR: [ADR-0002 인증 및 리프레시 토큰 처리](../adr/0002-auth-refresh-token.md)

## 인증 방식

| 항목 | 값 |
|------|----|
| 방식 | JWT (Bearer) |
| Access Token TTL | 30분 |
| Refresh Token TTL | 7일 |
| Refresh Token 저장 위치 | `tb_user.refresh_token` (DB, 사용자당 1개) |
| Refresh Token Rotation | 미적용 — refresh 시 access token만 재발급 |
| 단일 세션 | 새 로그인 시 refresh_token 덮어씀 → 기존 세션 종료 |

## 토큰 생명주기

```
로그인 성공
  → access token(30분) + refresh token(7일) 발급
  → refresh_token을 tb_user에 저장

access token 만료
  → refresh token으로 /auth/refresh 요청
  → 서버는 DB의 refresh_token과 비교 후 새 access token 발급

로그아웃
  → tb_user.refresh_token = NULL (즉시 무효화)

새 로그인 (기존 세션 있음)
  → refresh_token 덮어씀 → 이전 refresh token 무효화
```

## 인가 (Authorization)

- 인가 방식: **소유권 기반 격리** — 역할(Role)/RBAC 없음
- 모든 장부 조회·집계·가이드 생성은 인증된 `user_no` 본인 데이터에만 접근한다
- 다른 사용자의 `user_no`로 접근하는 요청은 서비스 레이어에서 차단한다

## Spring Security 필터 체인 규칙

| 경로 | 접근 |
|------|------|
| `POST /auth/login` | 인증 없이 허용 |
| `POST /auth/refresh` | 인증 없이 허용 |
| 그 외 모든 경로 | JWT 인증 필수 |

- JWT 검증은 **Security 필터에서 진입 시 1회**만 수행한다
- 검증 성공 시 `user_no`를 `SecurityContext`에 저장 → 이후 레이어에서 재검증 불필요
- 필터에서 검증 실패 시 `401 Unauthorized` 반환, 서비스 레이어까지 진입하지 않는다

## JWT 패키지 구현 위치

```
infra/security/
├── SecurityConfig.java        — 필터 체인 정의, 경로별 접근 규칙
└── jwt/
    ├── JwtProvider.java       — 토큰 발급·파싱·검증
    ├── JwtAuthFilter.java     — OncePerRequestFilter, SecurityContext 설정
    └── JwtProperties.java     — secret, TTL 설정값 바인딩
```

## 구현 체크리스트

- [ ] `JwtProvider`: access/refresh 발급, 파싱, 만료 검증
- [ ] `JwtAuthFilter`: Authorization 헤더 추출 → 검증 → `SecurityContextHolder` 설정
- [ ] `UserRepository.findByRefreshToken()`: refresh 재발급 시 DB 토큰 비교용
- [ ] 로그인/로그아웃/refresh 서비스: `tb_user.refresh_token` 갱신·초기화
- [ ] 모든 Service 메서드: `SecurityContext`에서 `user_no` 추출 후 쿼리 격리

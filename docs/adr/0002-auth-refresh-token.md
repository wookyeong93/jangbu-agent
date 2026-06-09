# ADR-0002: 인증 및 리프레시 토큰 처리

## 맥락
개인 소상공인이 자기 장부만 보는 단일 사용자 단위 서비스다. 개인 토이 프로젝트라 다중 기기 동시 세션 요구사항은 없다.
스키마(tb_user.refresh_token, unique)는 리프레시 토큰을 사용자당 하나, 서버(DB)에 저장하는 구조를 이미 전제한다.

## 결정
- 인증: JWT. access token으로 API 인증, refresh token으로 access 재발급.
- refresh token은 tb_user.refresh_token에 서버 저장 → 서버에서 무효화 가능.
- 사용자당 refresh token 1개. 새 로그인 시 덮어씀 = 단일 세션.
- 로그아웃 시 refresh_token 컬럼을 비워 즉시 무효화.
- 인가는 소유권 기반 격리: 모든 요청은 인증된 user_no 본인 데이터에만 접근 (역할/RBAC 없음).
- TTL: access 30분 / refresh 1주(7일).
- rotation: 미적용. refresh 시 access token만 재발급, refresh token은 만료까지 유지.
- **refresh token DB 저장 방식: SHA-256 해시로 변환 후 저장.**
  - 생성: `UUID` (opaque token) → 클라이언트에 반환
  - 저장: `SHA-256(UUID)` hex 64자 → `tb_user.refresh_token`
  - 검증: 클라이언트 전송값을 SHA-256 → DB `WHERE refresh_token = ?` 조회
  - SHA-256을 선택한 이유:
    1. **결정론적** (같은 입력 → 같은 출력) → 해시값으로 DB `WHERE` 조회 가능.
       BCrypt는 매번 다른 salt를 사용해 비결정론적이므로 DB 직접 조회 불가 → 부적합.
    2. **DB 탈취 시 토큰 재사용 불가** → DB에는 해시값만 저장되므로 원본 UUID를 역산할 수 없어
       탈취한 해시값으로 재발급 요청을 보내도 클라이언트가 보내는 원본 UUID와 일치하지 않음.
- **refresh token 전달 방식: HttpOnly 쿠키.**
  - 로그인 응답 body에는 access token만 포함. refresh token은 response body에 담지 않는다.
  - 서버가 `Set-Cookie: refreshToken=<UUID>; HttpOnly; Secure; SameSite=Strict; Path=/api/auth` 로 설정.
  - 재발급·로그아웃 요청 시 브라우저가 쿠키를 자동 전송, 서버에서 쿠키로 읽는다.
  - HttpOnly 를 선택한 이유: response body 에 담으면 JS 로 읽힐 수 있어 XSS 공격 시 탈취 가능.
    HttpOnly 쿠키는 JS 접근이 원천 차단되므로 XSS 로 탈취 불가.

## 고려한 대안
- 무상태 JWT-only(서버 저장 X): 단순하나 토큰 무효화 불가 → 로그아웃·탈취 대응 어려움
- refresh token rotation + 재사용 감지: 보안 강하나 개인 단일 세션엔 과한 복잡도
- 다중 세션(기기별 토큰 테이블): 요구사항 없음, 스키마도 단일 컬럼 구조

## 결과
- (+) 서버 저장으로 로그아웃·강제 무효화 가능, 구조 단순
- (+) 단일 세션이라 토큰 관리 로직 단순
- (−) 다중 기기 동시 로그인 불가 (새 로그인 시 기존 세션 종료)
- (−) rotation 미적용 → refresh token 탈취 시 만료까지 유효. TTL을 과도하게 길게 두지 않음으로 완화
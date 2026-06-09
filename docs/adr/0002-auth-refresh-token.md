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

## 고려한 대안
- 무상태 JWT-only(서버 저장 X): 단순하나 토큰 무효화 불가 → 로그아웃·탈취 대응 어려움
- refresh token rotation + 재사용 감지: 보안 강하나 개인 단일 세션엔 과한 복잡도
- 다중 세션(기기별 토큰 테이블): 요구사항 없음, 스키마도 단일 컬럼 구조

## 결과
- (+) 서버 저장으로 로그아웃·강제 무효화 가능, 구조 단순
- (+) 단일 세션이라 토큰 관리 로직 단순
- (−) 다중 기기 동시 로그인 불가 (새 로그인 시 기존 세션 종료)
- (−) rotation 미적용 → refresh token 탈취 시 만료까지 유효. TTL을 과도하게 길게 두지 않음으로 완화
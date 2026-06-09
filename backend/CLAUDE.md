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

## 테스트
- 집계·계산 로직은 단위 테스트 필수. 경계값(0건, 단일건, 월 경계) 포함.
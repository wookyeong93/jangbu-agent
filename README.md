# 장부 에이전트
소상공인을 위한 매입/판매 장부 + AI 매입 가이드 에이전트

## 1. 문제 정의
- 개인 소상공인의 경우 대부분 순수익을 감으로 계산함 
- 감으로 계산 하기 때문에 정확한 마진과 순수익을 확인하기 어려움

## 2. 핵심 기능
- 장부: 매입/판매/지출 입력 → 마진·순익 자동 계산
- 대시보드: 월별·요일별 매출 현황
- AI 에이전트: 매출이 높은 요일 자동 가이드 / 매출에 따른 매입량 가이드 

## 3. 아키텍처 — Agent = Model + Harness
이 시스템은 model(LLM) 과 Harness(통제 계층) 로 분리 된다.
Harness 가 숫자 계산 , 데이터 조회 검증을 책임지고 
LLM은 숫자의 계산,예측을 신뢰할 수 없기 때문에 그 결과를 해석해 조언만 생성한다 . 

### Model (LLM)
- 매입/매출 데이터 해석 및 자연어 조언 생성
- 어떤 데이터가 필요한지(어떤 툴을 부를지) 제안

### Harness (통제 계층)
- Tool: 장부·집계 데이터 조회 (정확한 수치는 SQL/코드가 계산)
- Guardrail: 사용자 권한 검증 / 숫자는 반드시 툴 결과만 사용(환각 차단)
- Eval: 고정 시나리오로 조언 품질 회귀 테스트
- Observability: 모든 툴 호출·토큰·응답시간 로깅(trace id)

### 처리 흐름
일일 배치
→ Harness가 툴로 매입·매출 데이터 조회 
→ Model이 해석·조언 생성 
→ Guardrail 검증 
→ 가이드 표시

## 4. 기술 스택
**Backend** (REST API)
- Java 25: 최신 LTS 적용하여 안정성 확보
- Spring Boot 4: 최신 안정화 버전 사용
- JPA + QueryDSL: 단순 CRUD 및 타입 세이프한 동적 쿼리 처리를 위한 선택
- MyBatis: JPA의 약점인 복잡한 통계 쿼리 조회를 위한 선택
- PostgreSQL: 분석·통계 쿼리(윈도우 함수 등) 지원이 강한 오픈소스 RDBMS
- Spring Security + JWT: 사용자별 장부 격리를 위한 인증
- OpenAI API: 매입/매출 데이터 기반 조언 생성

**Frontend** (별도 모듈)
- Vue (대시보드, API 소비)

## 5. AI 활용 워크플로우

> AI 연결은 빌드 순서상 마지막 단계. 현재 Harness(Tool/Guardrail) 구현 완료, LLM 연결 예정.

### 데이터 흐름
```
[사용자 장부 입력]
       ↓
[Harness — Tool]
매입·매출·지출 집계 조회 (결정론적 SQL) / 순이익·마진율 계산
       ↓
[Harness — Guardrail]
user_no 격리 검증 / 수치 출처 검증 (환각 차단)
       ↓
[Model — OpenAI]
집계 결과를 자연어 조언으로 해석
"이번 주 매입을 X% 줄이면 순익 Y 예상" 형태 가이드 생성
       ↓
[Harness — 저장]
tb_daily_guide에 기준 수치 스냅샷 + 조언 내용 저장
       ↓
[사용자 — 가이드 표시]
```

### 핵심 규칙
- LLM은 수치를 직접 계산·추정하지 않는다. 모든 숫자는 Tool 실행 결과에서만 온다.
- LLM이 수치를 생성하면 Guardrail이 거부한다.
- 가이드 생성 시 기준 수치(매입/매출/지출/순익)를 `tb_daily_guide`에 스냅샷 저장.

## 6. 실행 방법

### 사전 준비
- Java 25
- PostgreSQL 실행 후 DB 생성

```sql
CREATE DATABASE jangbu;
```

### DB 초기화
```bash
# 스키마 생성
psql -U <user> -d jangbu -f db/schema_ddl.sql

# 공통 코드 초기 데이터 (trxType 등)
psql -U <user> -d jangbu -f db/db_dump.sql
```

### 환경 변수
| 변수 | 설명 | 기본값 |
|---|---|---|
| `DB_USERNAME` | DB 사용자명 | `jangbu` |
| `DB_PASSWORD` | DB 비밀번호 | `jangbu` |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) | *(운영 환경에서 반드시 변경)* |

### 백엔드 실행
```bash
cd backend
./gradlew bootRun
```
서버 기동: `http://localhost:8080`

## 7. 주요 의사결정

| # | 제목 | 결정 요약 |
|---|---|---|
| ADR-0001 | 영속성 프레임워크 분리 | 단순 CRUD → JPA+QueryDSL / 통계 집계 → MyBatis |
| ADR-0002 | 인증·리프레시 토큰 처리 | JWT + refresh token DB 저장(SHA-256 해시), HttpOnly 쿠키 전달 |
| ADR-0003 | user 삭제 시 장부 데이터 처리 | ON DELETE CASCADE — 토이 프로젝트 범위, 거래기록 보존 안 함 |

상세 내용: [`docs/adr/`](docs/adr/)
# ADR-0004: AI 가이드 LLM 제공자를 OpenAI에서 Gemini로 교체

## 맥락
- AI 가이드 기능(Step 3-4)은 처음에 OpenAI Chat Completions API(`gpt-4o-mini`)로 연결했다.
- 개인 토이 프로젝트라 결제 없이 무료로 운영하고 싶었는데, OpenAI API는 ChatGPT 구독과 별개로 결제수단 등록과 크레딧 충전이 필요한 유료 API라 `insufficient_quota` 429로 막혔다.
- Google Gemini API는 Flash 계열 모델에 한해 결제수단 등록 없이 쓸 수 있는 무료 티어(분당 10회, 일 1,500회 등 제한)를 제공한다.
- 가이드 텍스트는 3~5문장의 짧은 해석 텍스트만 생성하므로, 무료 티어 트래픽 한도로도 충분하다.

## 결정
- AI 가이드 생성용 LLM 클라이언트를 OpenAI에서 Gemini(`gemini-2.5-flash`)로 교체한다.
- `infra/ai/OpenAiClient`·`OpenAiConfig`·`OpenAiProperties`를 삭제하고 `GeminiClient`·`GeminiConfig`·`GeminiProperties`로 대체한다.
- 인증은 `x-goog-api-key` 헤더, 요청/응답은 Gemini `generateContent` 스키마(`contents`/`systemInstruction`/`generationConfig`)를 따른다.
- Gemini 2.5 계열의 기본 "thinking" 모드는 `generationConfig.thinkingConfig.thinkingBudget=0`으로 비활성화한다 — thinking 토큰이 응답 토큰과 같은 예산을 공유해 답변이 중간에 잘리는 문제가 있었다 (Eval 하니스로 발견).

## 고려한 대안
1. **OpenAI 유지 + 결제수단 등록 (기각)**
   - 가장 간단하지만 토이 프로젝트에 실비용이 발생
2. **Gemini로 교체 (채택)**
   - 무료 티어로 운영 가능, 응답 포맷이 달라 클라이언트 재작성 필요
3. **로컬 모델(Ollama 등)로 교체 (기각)**
   - 완전 무료지만 로컬 GPU/리소스 의존, 배포 환경 고려 시 복잡도 증가

## 결과
- (+) 결제 없이 무료로 AI 가이드 기능 운영 가능
- (+) Eval 하니스 도입 계기가 되어 응답 잘림·Guardrail 오탐 버그 3건을 함께 발견·수정함
- (-) Pro 계열 모델은 무료 티어에서 제외되므로(2026-04 정책 변경) Flash 계열에 고정됨 — 응답 품질이 OpenAI `gpt-4o-mini` 대비 다를 수 있음
- (-) 무료 티어 한도(분당 10회, 일 1,500회) 초과 시 별도 처리 없음 — 트래픽이 늘면 재검토 필요

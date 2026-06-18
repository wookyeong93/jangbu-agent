package com.wookyeong.jangbu_agent.infra.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wookyeong.jangbu_agent.domain.guide.dto.GuideContextDto;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Gemini generateContent API 클라이언트.
 *
 * <p>LLM 호출은 이 클래스가 단일 책임으로 담당한다.
 * 수치 계산은 하지 않는다 — {@link GuideContextDto} 를 받아 해석 텍스트만 생성한다.
 */
@Component
public class GeminiClient {

    private static final String SYSTEM_PROMPT = """
            당신은 소상공인 매입/판매 장부 분석 도우미입니다.
            아래 집계 데이터를 바탕으로 ① 매입 시점 조언 ② 매출 피크 활용 방법 ③ 매입 금액 조정 의견을 한국어로 작성하세요.
            반드시 제공된 수치만 인용하고, 절대 숫자를 추정하거나 만들어 내지 마세요.
            3~5문장으로 간결하게 작성하세요.
            """;

    private static final String[] DOW_NAMES = {"일", "월", "화", "수", "목", "금", "토"};

    private final RestClient restClient;
    private final GeminiProperties props;

    public GeminiClient(GeminiProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-goog-api-key", props.getApiKey())
                .build();
    }

    public String generateGuide(GuideContextDto ctx) {
        GenerateContentRequest request = new GenerateContentRequest(
                new GenerateContentRequest.SystemInstruction(
                        List.of(new GenerateContentRequest.Part(SYSTEM_PROMPT))),
                List.of(new GenerateContentRequest.Content(
                        "user", List.of(new GenerateContentRequest.Part(buildUserPrompt(ctx))))),
                new GenerateContentRequest.GenerationConfig(0.3, 600)
        );

        GenerateContentResponse response = restClient.post()
                .uri("/models/{model}:generateContent", props.getModel())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GenerateContentResponse.class);

        if (response == null || response.candidates().isEmpty()) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }
        return response.candidates().get(0).content().parts().get(0).text();
    }

    public String getModelName() {
        return props.getModel();
    }

    private String buildUserPrompt(GuideContextDto ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append("[최근 28일 요약]\n");
        sb.append("총매입: ").append(String.format("%,d", ctx.getTotalPurchase())).append("원 | ");
        sb.append("총매출: ").append(String.format("%,d", ctx.getTotalSale())).append("원 | ");
        sb.append("총지출: ").append(String.format("%,d", ctx.getTotalExpense())).append("원 | ");
        sb.append("순익: ").append(String.format("%,d", ctx.getNetProfit())).append("원");
        if (ctx.getMarginRate() != null) {
            sb.append(" | 마진율: ").append(ctx.getMarginRate()).append("%");
        }

        if (ctx.getWeekdaySalesTrend() != null && !ctx.getWeekdaySalesTrend().isEmpty()) {
            sb.append("\n\n[요일별 평균 매출]\n");
            for (WeekdaySalesResult w : ctx.getWeekdaySalesTrend()) {
                sb.append(DOW_NAMES[w.getDow()]).append("요일: ")
                        .append(String.format("%,d", w.getAvgSale())).append("원  ");
            }
        }

        sb.append("\n\n[매입 주기 분석]\n");
        if (ctx.getAvgCycleDays() != null) {
            sb.append("평균 매입 주기: ").append(ctx.getAvgCycleDays()).append("일 | ");
            sb.append("마지막 매입: ").append(ctx.getDaysSinceLastPurchase()).append("일 전 (")
                    .append(ctx.getLastPurchaseDate()).append(") | ");
            sb.append("다음 예상 매입일: ").append(ctx.getNextExpectedDate());
        } else if (ctx.getLastPurchaseDate() != null) {
            sb.append("마지막 매입: ").append(ctx.getDaysSinceLastPurchase())
                    .append("일 전 | 주기 계산 불가 (데이터 부족)");
        } else {
            sb.append("매입 이력 없음");
        }

        return sb.toString();
    }

    // ── Gemini 요청·응답 내부 레코드 ──────────────────────────────────────────

    record GenerateContentRequest(
            @JsonProperty("systemInstruction") SystemInstruction systemInstruction,
            List<Content> contents,
            @JsonProperty("generationConfig") GenerationConfig generationConfig
    ) {
        record SystemInstruction(List<Part> parts) {}
        record Content(String role, List<Part> parts) {}
        record Part(String text) {}
        record GenerationConfig(
                double temperature,
                @JsonProperty("maxOutputTokens") int maxOutputTokens
        ) {}
    }

    record GenerateContentResponse(List<Candidate> candidates) {
        record Candidate(Content content) {}
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }
}

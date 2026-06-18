package com.wookyeong.jangbu_agent.domain.guide.service;

import com.wookyeong.jangbu_agent.domain.guide.dto.GuideContextDto;
import com.wookyeong.jangbu_agent.domain.guide.dto.WeekdaySalesResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM이 생성한 가이드 텍스트에 {@link GuideContextDto}에 없는 수치가 등장하는지 검증한다.
 *
 * <p>1자리 숫자는 목록 번호("1)", "2)") 등으로 흔히 쓰이므로 검증 대상에서 제외한다 —
 * 그렇지 않으면 정상 응답까지 과도하게 차단된다.
 */
@Component
public class GuideGuardrail {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?");

    /** 컨텍스트에 없는 숫자(2자리 이상)를 모두 찾아 반환한다. 비어있으면 안전한 응답이다. */
    public Set<String> findViolations(String guideText, GuideContextDto ctx) {
        Set<String> allowed = allowedNumbers(ctx);
        Set<String> violations = new LinkedHashSet<>();

        Matcher matcher = NUMBER_PATTERN.matcher(guideText);
        while (matcher.find()) {
            String token = matcher.group().replace(",", "");
            if (token.replace(".", "").length() < 2) {
                continue;
            }
            if (!allowed.contains(token)) {
                violations.add(matcher.group());
            }
        }
        return violations;
    }

    private Set<String> allowedNumbers(GuideContextDto ctx) {
        Set<String> allowed = new HashSet<>();
        allowed.add(String.valueOf(ctx.getTotalPurchase()));
        allowed.add(String.valueOf(ctx.getTotalSale()));
        allowed.add(String.valueOf(ctx.getTotalExpense()));
        allowed.add(String.valueOf(Math.abs(ctx.getNetProfit())));

        if (ctx.getMarginRate() != null) {
            double abs = Math.abs(ctx.getMarginRate());
            allowed.add(String.valueOf(abs));
            if (abs == Math.floor(abs)) {
                allowed.add(String.valueOf((long) abs));
            }
        }
        if (ctx.getAvgCycleDays() != null) {
            allowed.add(String.valueOf(ctx.getAvgCycleDays()));
        }
        if (ctx.getDaysSinceLastPurchase() != null) {
            allowed.add(String.valueOf(ctx.getDaysSinceLastPurchase()));
        }
        if (ctx.getWeekdaySalesTrend() != null) {
            for (WeekdaySalesResult w : ctx.getWeekdaySalesTrend()) {
                allowed.add(String.valueOf(w.getAvgSale()));
            }
        }
        addDateNumbers(allowed, ctx.getLastPurchaseDate());
        addDateNumbers(allowed, ctx.getNextExpectedDate());

        return allowed;
    }

    private void addDateNumbers(Set<String> allowed, LocalDate date) {
        if (date == null) {
            return;
        }
        allowed.add(String.valueOf(date.getYear()));
        allowed.add(String.valueOf(date.getMonthValue()));
        allowed.add(String.format("%02d", date.getMonthValue()));
        allowed.add(String.valueOf(date.getDayOfMonth()));
        allowed.add(String.format("%02d", date.getDayOfMonth()));
    }
}

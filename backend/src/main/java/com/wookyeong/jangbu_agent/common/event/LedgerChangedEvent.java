package com.wookyeong.jangbu_agent.common.event;

/**
 * 장부(매입·매출·지출) 생성·수정·삭제 시 발행되는 이벤트.
 *
 * <p>도메인 간 직접 의존을 금지하는 규칙(backend CLAUDE.md) 때문에 ledger 도메인이
 * guide 도메인을 직접 호출하지 않고 이 이벤트로만 변경을 알린다.
 */
public record LedgerChangedEvent(Integer userNo) {
}

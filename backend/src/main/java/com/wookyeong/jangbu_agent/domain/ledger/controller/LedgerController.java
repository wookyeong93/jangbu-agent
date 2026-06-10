package com.wookyeong.jangbu_agent.domain.ledger.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerCreateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerMonthlyResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerResponse;
import com.wookyeong.jangbu_agent.domain.ledger.dto.LedgerUpdateRequest;
import com.wookyeong.jangbu_agent.domain.ledger.service.LedgerService;
import com.wookyeong.jangbu_agent.infra.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 장부 CRUD 엔드포인트. JWT 인증 필수.
 *
 * <p>모든 요청은 {@link UserPrincipal#getUserNo()} 로 격리된다.
 */
@Tag(name = "장부", description = "매입·매출·지출 장부 CRUD")
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Validated
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<ApiResponse<LedgerResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LedgerCreateRequest request) {
        LedgerResponse response = ledgerService.create(principal.getUserNo(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{ledgerNo}")
    public ResponseEntity<ApiResponse<LedgerResponse>> findOne(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ledgerNo) {
        LedgerResponse response = ledgerService.findOne(principal.getUserNo(), ledgerNo);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LedgerMonthlyResponse>> findMonthly(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month) {
        LedgerMonthlyResponse response = ledgerService.findMonthly(principal.getUserNo(), year, month);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{ledgerNo}")
    public ResponseEntity<ApiResponse<LedgerResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ledgerNo,
            @Valid @RequestBody LedgerUpdateRequest request) {
        LedgerResponse response = ledgerService.update(principal.getUserNo(), ledgerNo, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{ledgerNo}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long ledgerNo) {
        ledgerService.delete(principal.getUserNo(), ledgerNo);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}

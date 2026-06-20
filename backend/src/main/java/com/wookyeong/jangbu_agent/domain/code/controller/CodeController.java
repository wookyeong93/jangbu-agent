package com.wookyeong.jangbu_agent.domain.code.controller;

import com.wookyeong.jangbu_agent.common.response.ApiResponse;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.code.dto.CodeResponse;
import com.wookyeong.jangbu_agent.domain.code.dto.GroupCodeResponse;
import com.wookyeong.jangbu_agent.domain.code.service.CodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공통 코드 조회 엔드포인트. JWT 인증 필수.
 */
@Tag(name = "공통 코드", description = "그룹 코드 및 하위 코드 조회")
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @Operation(summary = "전체 코드 조회", description = "모든 그룹 코드와 그룹별 하위 코드 목록을 반환한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupCodeResponse>>> findAll() {
        List<GroupCodeResponse> groups = codeService.findAll();
        if (groups.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail(ErrorCode.NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.ok(groups));
    }

    @Operation(summary = "그룹별 코드 조회", description = "지정한 그룹 코드의 하위 코드 목록만 반환한다.")
    @GetMapping("/{groupCode}")
    public ResponseEntity<ApiResponse<List<CodeResponse>>> findByGroupCode(@PathVariable String groupCode) {
        List<CodeResponse> codes = codeService.findByGroupCode(groupCode);
        if (codes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail(ErrorCode.NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.ok(codes));
    }
}

package com.wookyeong.jangbu_agent.domain.user.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.user.dto.LoginRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.SignupRequest;
import com.wookyeong.jangbu_agent.domain.user.dto.TokenResponse;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import com.wookyeong.jangbu_agent.infra.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 비즈니스 로직.
 *
 * <p>Refresh Token 흐름:
 * <ol>
 *   <li>로그인: UUID 생성 → 클라이언트에 반환, SHA-256(UUID) → DB 저장
 *   <li>재발급: 클라이언트 UUID 수신 → SHA-256 변환 → DB 조회 → Access Token 신규 발급
 *   <li>로그아웃: DB 의 refresh_token 컬럼 null 처리 → 즉시 무효화
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new BusinessException(ErrorCode.USER_ID_DUPLICATED);
        }
        User user = User.builder()
                .userId(request.getUserId())
                .userPwd(passwordEncoder.encode(request.getPassword()))
                .userNm(request.getUserNm())
                .build();
        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPwd())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String refreshToken = jwtTokenProvider.generateRefreshToken();
        user.updateRefreshToken(jwtTokenProvider.hashToken(refreshToken));

        return new TokenResponse(
                jwtTokenProvider.generateAccessToken(user.getUserId(), user.getUserNo()),
                refreshToken
        );
    }

    /**
     * Refresh Token 으로 Access Token 재발급.
     * Rotation 미적용 — Refresh Token 은 갱신하지 않는다 (ADR-0002 참고).
     */
    public TokenResponse reissue(String refreshToken) {
        String hashed = jwtTokenProvider.hashToken(refreshToken);
        User user = userRepository.findByRefreshToken(hashed)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        return new TokenResponse(
                jwtTokenProvider.generateAccessToken(user.getUserId(), user.getUserNo()),
                refreshToken
        );
    }

    public void logout(String userId) {
        userRepository.findByUserId(userId)
                .ifPresent(user -> user.updateRefreshToken(null));
    }
}

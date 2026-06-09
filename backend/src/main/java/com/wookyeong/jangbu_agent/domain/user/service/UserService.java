package com.wookyeong.jangbu_agent.domain.user.service;

import com.wookyeong.jangbu_agent.common.exception.BusinessException;
import com.wookyeong.jangbu_agent.common.response.ErrorCode;
import com.wookyeong.jangbu_agent.domain.user.dto.UpdateProfileRequest;
import com.wookyeong.jangbu_agent.domain.user.entity.User;
import com.wookyeong.jangbu_agent.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 사용자 프로필 관리 비즈니스 로직.
 *
 * <p>변경 가능 항목: 이름(userNm), 비밀번호.
 * 아이디(userId)는 변경 불가. 두 항목 모두 null 이면 INVALID_INPUT.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateProfile(Integer userNo, UpdateProfileRequest request) {
        boolean hasNm = StringUtils.hasText(request.getUserNm());
        boolean hasPw = StringUtils.hasText(request.getNewPassword());

        if (!hasNm && !hasPw) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (hasNm) {
            user.updateUserNm(request.getUserNm());
        }

        if (hasPw) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getUserPwd())) {
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }
            if (passwordEncoder.matches(request.getNewPassword(), user.getUserPwd())) {
                throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
            }
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }
}

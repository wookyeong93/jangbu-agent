package com.wookyeong.jangbu_agent.domain.user.entity;

import com.wookyeong.jangbu_agent.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티 (tb_user).
 *
 * <p>장부의 모든 데이터는 user_no 로 격리된다.
 * 서비스 레이어에서 반드시 현재 로그인 사용자의 user_no 로만 조회/수정할 것.
 */
@Entity
@Table(name = "tb_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Integer userNo;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    /** BCrypt 해시 저장. 평문 비밀번호를 직접 넣지 말 것. */
    @Column(name = "user_pwd", nullable = false, length = 255)
    private String userPwd;

    @Column(name = "user_nm", length = 100)
    private String userNm;

    /** JWT Refresh Token. 재발급 시 갱신, 로그아웃 시 null 로 초기화. */
    @Column(name = "refresh_token", unique = true, length = 255)
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePassword(String encodedPassword) {
        this.userPwd = encodedPassword;
    }
}

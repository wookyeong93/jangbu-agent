package com.wookyeong.jangbu_agent.domain.user.repository;

import com.wookyeong.jangbu_agent.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 데이터 접근.
 *
 * <p>{@code findByRefreshToken} 의 파라미터는 SHA-256 해시값이어야 한다.
 * 원본 UUID 를 그대로 넘기지 말 것.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserId(String userId);

    /** {@code hashedToken} = SHA-256(원본 UUID). */
    Optional<User> findByRefreshToken(String hashedToken);

    boolean existsByUserId(String userId);
}

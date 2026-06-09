package com.wookyeong.jangbu_agent.infra.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * SecurityContext 에서 꺼내 쓰는 인증 주체.
 *
 * <p>JWT 클레임에서 파싱한 {@code userNo} 와 {@code userId} 를 함께 보유한다.
 * 서비스 레이어에서 DB 조회 없이 user_no 를 사용할 수 있다.
 *
 * <pre>
 * UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
 * Integer userNo = principal.getUserNo();
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Integer userNo;
    private final String userId;

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

package com.example.demo.service.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsPrincipleTest {

    @Test
    void builder_buildsUserDetailsWithEmailAndAuthorities() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));

        UserDetailsPrinciple principal = new UserDetailsPrinciple.Builder()
                .email("john@example.com")
                .authorities(authorities)
                .build();

        assertThat(principal.getEmail()).isEqualTo("john@example.com");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");
    }

    @Test
    void getPassword_returnsEmailForCurrentImplementation() {
        UserDetailsPrinciple principal = new UserDetailsPrinciple(
                "john@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThat(principal.getPassword()).isEqualTo("john@example.com");
    }

    @Test
    void getUsername_returnsNullForCurrentImplementation() {
        UserDetailsPrinciple principal = new UserDetailsPrinciple(
                "john@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThat(principal.getUsername()).isNull();
    }

    @Test
    void defaultAccountStatusFlags_areTrue() {
        UserDetailsPrinciple principal = new UserDetailsPrinciple("john@example.com", List.of());

        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }
}

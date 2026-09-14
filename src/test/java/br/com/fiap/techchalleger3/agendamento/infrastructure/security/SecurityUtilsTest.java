package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    private Jwt jwtBasico() {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("sub", "uuid-teste")
                .build();
    }

    @Test
    void isAdmin_comRoleAdmin_retornaTrue() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwtBasico(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.isAdmin(token)).isTrue();
    }

    @Test
    void isAdmin_semRoleAdmin_retornaFalse() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwtBasico(), List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));

        assertThat(SecurityUtils.isAdmin(token)).isFalse();
    }

    @Test
    void hasProfissionalRole_comRoleProfissional_retornaTrue() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwtBasico(), List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));

        assertThat(SecurityUtils.hasProfissionalRole(token)).isTrue();
    }

    @Test
    void hasProfissionalRole_semRoleProfissional_retornaFalse() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwtBasico(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.hasProfissionalRole(token)).isFalse();
    }
}

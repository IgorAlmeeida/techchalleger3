package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static boolean isAdmin(JwtAuthenticationToken principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static boolean hasProfissionalRole(JwtAuthenticationToken principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PROFISSIONAL".equals(a.getAuthority()));
    }
}

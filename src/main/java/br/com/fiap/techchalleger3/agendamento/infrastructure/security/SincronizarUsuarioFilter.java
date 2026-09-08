package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import br.com.fiap.techchalleger3.agendamento.application.usecase.SincronizarUsuarioUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SincronizarUsuarioFilter extends OncePerRequestFilter {

    private final SincronizarUsuarioUseCase sincronizarUsuarioUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String sub = jwt.getSubject();
                RoleEnum role = extrairRole(jwt);
                String nome = jwt.getClaimAsString("name");
                if (nome == null || nome.isBlank()) {
                    nome = jwt.getClaimAsString("preferred_username");
                }
                if (role != null) {
                    sincronizarUsuarioUseCase.executar(sub, role, nome);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao sincronizar usuario: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private RoleEnum extrairRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) return null;
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) return null;
        for (String r : roles) {
            try {
                return RoleEnum.obterPorCodigo(r);
            } catch (IllegalArgumentException ignored) {
                // role string not recognized — try next role in the list
            }
        }
        return null;
    }
}

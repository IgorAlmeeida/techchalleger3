package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextoEstabelecimentoFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Estabelecimento-Id";

    private static final Set<String> PATHS_SEM_CONTEXTO = Set.of(
            "/api/auth", "/api/contexto", "/swagger-ui", "/v3/api-docs", "/actuator"
    );

    private final ContextoUsuario contextoUsuario;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PATHS_SEM_CONTEXTO.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && !header.isBlank()) {
            try {
                contextoUsuario.setEstabelecimentoId(Integer.parseInt(header));
            } catch (NumberFormatException e) {
                log.warn("Header {} inválido: {}", HEADER, header);
            }
        }
        filterChain.doFilter(request, response);
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import br.com.fiap.techchalleger3.agendamento.application.usecase.SincronizarUsuarioUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    // ── SincronizarUsuarioFilter ───────────────────────────────────────────

    @Mock SincronizarUsuarioUseCase sincronizarUseCase;
    @InjectMocks SincronizarUsuarioFilter sincronizarFilter;

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Jwt jwt(String sub, String name, Map<String, Object> realmAccess) {
        return Jwt.withTokenValue("tok")
                .header("alg", "RS256")
                .subject(sub)
                .claim("name", name)
                .claim("realm_access", realmAccess)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    void sincronizar_comRoleCliente_chamaSincronizar() throws Exception {
        Jwt jwt = jwt("sub-1", "João", Map.of("roles", List.of("CLIENTE")));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(sincronizarUseCase).executar(any(), any(), any());
        verify(chain).doFilter(req, res);
    }

    @Test
    void sincronizar_semAuthentication_naoChama() throws Exception {
        SecurityContextHolder.clearContext();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(sincronizarUseCase, never()).executar(any(), any(), any());
        verify(chain).doFilter(req, res);
    }

    @Test
    void sincronizar_roleDesconhecida_naoChama() throws Exception {
        Jwt jwt = jwt("sub-1", "User", Map.of("roles", List.of("UNKNOWN_ROLE")));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(sincronizarUseCase, never()).executar(any(), any(), any());
        verify(chain).doFilter(req, res);
    }

    @Test
    void sincronizar_semRealmAccess_naoChama() throws Exception {
        Jwt jwt = jwt("sub-1", "User", null);
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(sincronizarUseCase, never()).executar(any(), any(), any());
    }

    @Test
    void sincronizar_nomeNulo_usaPreferredUsername() throws Exception {
        Jwt jwt = Jwt.withTokenValue("tok")
                .header("alg", "RS256")
                .subject("sub-2")
                .claim("preferred_username", "pref-user")
                .claim("realm_access", Map.of("roles", List.of("PROFISSIONAL")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(sincronizarUseCase).executar(any(), any(), any());
    }

    @Test
    void sincronizar_excecaoInterna_continuaChain() throws Exception {
        Jwt jwt = jwt("sub-1", "João", Map.of("roles", List.of("CLIENTE")));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(sincronizarUseCase.executar(any(), any(), any())).thenThrow(new RuntimeException("err"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        sincronizarFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    // ── ContextoEstabelecimentoFilter ──────────────────────────────────────

    @Mock ContextoUsuario contextoUsuario;
    @InjectMocks ContextoEstabelecimentoFilter contextoFilter;

    @Test
    void contextFilter_shouldNotFilter_authPath() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/auth/login");

        assertThat(contextoFilter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void contextFilter_shouldFilter_servicosPath() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/servicos");

        assertThat(contextoFilter.shouldNotFilter(req)).isFalse();
    }

    @Test
    void contextFilter_comHeader_defineContexto() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("X-Estabelecimento-Id")).thenReturn("42");

        contextoFilter.doFilterInternal(req, res, chain);

        verify(contextoUsuario).setEstabelecimentoId(42);
        verify(chain).doFilter(req, res);
    }

    @Test
    void contextFilter_headerInvalido_naoLancaExcecao() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("X-Estabelecimento-Id")).thenReturn("nao-e-numero");

        contextoFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    @Test
    void contextFilter_semHeader_naoPropaga() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("X-Estabelecimento-Id")).thenReturn(null);

        contextoFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    // ── SecurityUtils ──────────────────────────────────────────────────────

    @Test
    void securityUtils_isAdmin_true() {
        Jwt jwt = jwt("sub", "u", Map.of());
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.isAdmin(principal)).isTrue();
    }

    @Test
    void securityUtils_isAdmin_false() {
        Jwt jwt = jwt("sub", "u", Map.of());
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));

        assertThat(SecurityUtils.isAdmin(principal)).isFalse();
    }

    @Test
    void securityUtils_hasProfissionalRole_true() {
        Jwt jwt = jwt("sub", "u", Map.of());
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));

        assertThat(SecurityUtils.hasProfissionalRole(principal)).isTrue();
    }

    @Test
    void securityUtils_hasProfissionalRole_false() {
        Jwt jwt = jwt("sub", "u", Map.of());
        JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.hasProfissionalRole(principal)).isFalse();
    }
}

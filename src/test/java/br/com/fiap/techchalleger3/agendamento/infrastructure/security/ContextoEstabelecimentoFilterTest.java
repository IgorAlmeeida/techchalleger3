package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ContextoEstabelecimentoFilterTest {

    @Mock private ContextoUsuario contextoUsuario;
    @Mock private FilterChain filterChain;
    @InjectMocks private ContextoEstabelecimentoFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldNotFilter_pathApiAuth_retornaTrue() {
        request.setRequestURI("/api/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_pathSwaggerUi_retornaTrue() {
        request.setRequestURI("/swagger-ui/index.html");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_pathApiContexto_retornaTrue() {
        request.setRequestURI("/api/contexto/me");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_pathV3ApiDocs_retornaTrue() {
        request.setRequestURI("/v3/api-docs/swagger-config");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_pathActuator_retornaTrue() {
        request.setRequestURI("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_pathAgendamentos_retornaFalse() {
        request.setRequestURI("/api/agendamentos");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterInternal_headerValido_setaEstabelecimentoId() throws Exception {
        request.setRequestURI("/api/agendamentos");
        request.addHeader("X-Estabelecimento-Id", "42");

        filter.doFilterInternal(request, response, filterChain);

        verify(contextoUsuario).setEstabelecimentoId(42);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_headerNulo_naoSetaEstabelecimentoId() throws Exception {
        request.setRequestURI("/api/agendamentos");

        filter.doFilterInternal(request, response, filterChain);

        verify(contextoUsuario, never()).setEstabelecimentoId(org.mockito.ArgumentMatchers.anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_headerEmBranco_naoSetaEstabelecimentoId() throws Exception {
        request.setRequestURI("/api/agendamentos");
        request.addHeader("X-Estabelecimento-Id", "   ");

        filter.doFilterInternal(request, response, filterChain);

        verify(contextoUsuario, never()).setEstabelecimentoId(org.mockito.ArgumentMatchers.anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_headerInvalido_naoSetaEContinuaChain() throws Exception {
        request.setRequestURI("/api/agendamentos");
        request.addHeader("X-Estabelecimento-Id", "nao-e-numero");

        filter.doFilterInternal(request, response, filterChain);

        verify(contextoUsuario, never()).setEstabelecimentoId(org.mockito.ArgumentMatchers.anyInt());
        verify(filterChain).doFilter(request, response);
    }
}

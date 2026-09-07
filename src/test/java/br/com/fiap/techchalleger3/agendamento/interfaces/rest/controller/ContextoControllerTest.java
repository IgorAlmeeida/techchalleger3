package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarEstabelecimentosContextoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContextoController.class)
@Import(SecurityConfig.class)
class ContextoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ListarEstabelecimentosContextoUseCase useCase;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SincronizarUsuarioFilter sincronizarFilter;
    @MockBean ContextoEstabelecimentoFilter contextoFilter;

    @BeforeEach
    void filtros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void listarEstabelecimentos_comAuth_retorna200() throws Exception {
        Estabelecimento est = Estabelecimento.builder()
                .id(1).nome("Clínica X").cnpj("12345678000195").endereco("Rua A, 1").build();
        when(useCase.executar(anyString())).thenReturn(List.of(est));

        mockMvc.perform(get("/api/contexto/estabelecimentos")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Clínica X"));
    }

    @Test
    void listarEstabelecimentos_listaVazia_retorna200() throws Exception {
        when(useCase.executar(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/contexto/estabelecimentos")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

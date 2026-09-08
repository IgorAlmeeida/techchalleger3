package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.BuscarEstabelecimentosUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.EstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EstabelecimentoResponseAssembler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstabelecimentoController.class)
@Import(SecurityConfig.class)
class EstabelecimentoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private EstabelecimentoUseCase useCase;
    @MockitoBean private EstabelecimentoResponseAssembler assembler;
    @MockitoBean private BuscarEstabelecimentosUseCase buscarEstabelecimentosUseCase;
    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockitoBean private ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @BeforeEach
    void configureFiltros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarUsuarioFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void deveRetornar401_quandoSemAutenticacaoNoCriar() throws Exception {
        mockMvc.perform(post("/api/estabelecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403_quandoRoleNaoAdmin() throws Exception {
        String body = """
                {
                    "nome": "Studio",
                    "cnpj": "12.345.678/0001-90",
                    "endereco": "Rua A",
                    "telefone": "11999999999",
                    "responsavelNome": "João",
                    "responsavelCpf": "123.456.789-09"
                }
                """;

        mockMvc.perform(post("/api/estabelecimentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar200_quandoBuscaComFiltrosSemParametros() throws Exception {
        when(buscarEstabelecimentosUseCase.buscar(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/estabelecimentos/buscar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk());
    }
}

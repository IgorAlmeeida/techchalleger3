package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.AvaliarAtendimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvaliacaoController.class)
@Import(SecurityConfig.class)
class AvaliacaoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AvaliarAtendimentoUseCase avaliarUseCase;
    @MockitoBean private AvaliacaoRepositoryPort avaliacaoPort;
    @MockitoBean private UsuarioRepositoryPort usuarioPort;
    @MockitoBean private ClienteRepositoryPort clientePort;
    @MockitoBean private ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @BeforeEach
    void configureFiltros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void deveRetornar401_quandoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400_quandoPayloadSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar403_quandoRoleIncorreta() throws Exception {
        String body = """
                {
                    "agendamentoId": 1,
                    "nota": 5
                }
                """;

        mockMvc.perform(post("/api/avaliacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar201_quandoAvaliarComSucesso() throws Exception {
        Usuario usuario = Usuario.builder().id(1).uuid("sub-cliente").build();
        Cliente cliente = Cliente.builder().id(10).build();
        Avaliacao avaliacao = Avaliacao.builder().id(1).agendamentoId(1).clienteId(10).nota(5).build();

        when(usuarioPort.buscarPorUuid("sub-cliente")).thenReturn(java.util.Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(java.util.Optional.of(cliente));
        when(avaliarUseCase.avaliar(anyInt(), anyInt(), anyInt(), any())).thenReturn(avaliacao);

        String body = """
                {
                    "agendamentoId": 1,
                    "nota": 5,
                    "comentario": "Muito bom"
                }
                """;

        mockMvc.perform(post("/api/avaliacoes")
                        .with(jwt().jwt(j -> j.subject("sub-cliente")).authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornar200_quandoListarPorEstabelecimento() throws Exception {
        when(avaliacaoPort.listarPorEstabelecimentoId(10)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/avaliacoes/estabelecimento/10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar200_quandoListarPorProfissionalVinculo() throws Exception {
        when(avaliacaoPort.listarPorProfissionalVinculoId(20)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/avaliacoes/profissional-vinculo/20")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk());
    }
}

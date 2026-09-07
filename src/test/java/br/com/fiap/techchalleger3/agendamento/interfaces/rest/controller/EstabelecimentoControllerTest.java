package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.BuscarEstabelecimentosUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.EstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EstabelecimentoResponseAssembler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstabelecimentoController.class)
@Import(SecurityConfig.class)
class EstabelecimentoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private EstabelecimentoUseCase useCase;
    @MockBean private EstabelecimentoResponseAssembler assembler;
    @MockBean private BuscarEstabelecimentosUseCase buscarEstabelecimentosUseCase;
    @MockBean private JwtDecoder jwtDecoder;
    @MockBean private SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockBean private ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

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

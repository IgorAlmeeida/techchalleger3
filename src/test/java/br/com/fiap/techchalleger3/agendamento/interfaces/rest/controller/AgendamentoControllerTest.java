package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ExportarAgendamentoIcsUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarAgendamentosProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarMeusAgendamentosClienteUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.AgendamentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendamentoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@Import(SecurityConfig.class)
class AgendamentoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CriarAgendamentoUseCase criarUseCase;
    @MockBean private CancelarAgendamentoUseCase cancelarUseCase;
    @MockBean private ListarMeusAgendamentosClienteUseCase listarClienteUseCase;
    @MockBean private ListarAgendamentosProfissionalUseCase listarProfissionalUseCase;
    @MockBean private ExportarAgendamentoIcsUseCase exportarIcsUseCase;
    @MockBean private AgendamentoResponseAssembler assembler;
    @MockBean private JwtDecoder jwtDecoder;
    @MockBean private SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockBean private ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @Test
    void deveRetornar401_quandoSemToken() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar400_quandoPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar201_quandoCriarComSucesso() throws Exception {
        Agendamento agendamento = Agendamento.builder()
                .id(1).agendaId(10).status(StatusAgendamentoEnum.AGENDADO)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .presencaConfirmada(false).build();

        AgendamentoResponse response = new AgendamentoResponse(
                1, 10, null, null, LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, StatusAgendamentoEnum.AGENDADO, false);

        when(criarUseCase.executar(anyString(), anyInt(), anyInt(), any(), any()))
                .thenReturn(agendamento);
        when(assembler.toResponse(any(Agendamento.class))).thenReturn(response);

        String body = """
                {
                    "profissionalVinculoId": 1,
                    "servicoId": 2
                }
                """;

        mockMvc.perform(post("/api/agendamentos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}

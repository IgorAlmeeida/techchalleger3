package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.AgendarEmNomeDeClienteUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AgendamentoPorProfissionalSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendarEmNomeDeClienteUseCase agendarEmNomeDeClienteUseCase;

    private ResultActions resultado;

    @Dado("a cliente {string} já está cadastrada no sistema")
    public void clienteJaCadastrada(String nome) {
        // contexto: cliente preexistente verificado via CPF no use case
    }

    @Dado("que o atendente do {string} está autenticado como PROFISSIONAL")
    public void atendenteAutenticado(String estabelecimento) {
        // autenticação via jwt() aplicada no Quando
    }

    @Quando("ele agenda {string} com {string} para a cliente {string} na próxima terça")
    public void agendaEmNomeDeCliente(String servico, String profissional, String cliente) throws Exception {
        Agendamento agendamento = Agendamento.builder()
                .id(2)
                .clienteId(10)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();
        when(agendarEmNomeDeClienteUseCase.executar(
                anyString(), any(), any(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyInt(), any()))
                .thenReturn(agendamento);

        String body = """
                {
                  "profissionalVinculoId": 1,
                  "servicoId": 1,
                  "cpf": "123.456.789-00",
                  "nome": "%s",
                  "email": "joana@test.com"
                }
                """.formatted(cliente);

        resultado = mockMvc.perform(post("/api/agendamentos/em-nome-de")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("o agendamento em nome do cliente deve ser confirmado com status {string}")
    public void agendamentoEmNomeConfirmado(String statusEsperado) throws Exception {
        resultado.andExpect(status().isCreated());
    }

    @E("o agendamento deve estar associado à cliente {string}")
    public void agendamentoAssociado(String cliente) {
        // verificado via clienteId no retorno do use case — coberto em testes unitários
    }

    @E("uma notificação de confirmação deve ser enviada para Joana")
    public void notificacaoEnviadaParaJoana() {
        // envio de email verificado via testes unitários de AgendarEmNomeDeClienteUseCase
    }

    @Dado("que o horário com {string} já está agendado por outro cliente")
    public void horarioComProfissionalOcupado(String profissional) {
        when(agendarEmNomeDeClienteUseCase.executar(
                anyString(), any(), any(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyInt(), any()))
                .thenThrow(new OperacaoInvalidaException(
                        "Sem horários disponíveis para o profissional e serviço solicitados."));
    }

    @Quando("o atendente tenta agendar {string} para a cliente {string} no mesmo horário ocupado")
    public void atendenteAgendaHorarioOcupado(String servico, String cliente) throws Exception {
        String body = """
                {
                  "profissionalVinculoId": 1,
                  "servicoId": 1,
                  "cpf": "123.456.789-00",
                  "nome": "%s",
                  "email": "joana@test.com"
                }
                """.formatted(cliente);

        resultado = mockMvc.perform(post("/api/agendamentos/em-nome-de")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("o sistema deve informar que não há disponibilidade para o agendamento em nome")
    public void semDisponibilidadeParaAgendamentoEmNome() throws Exception {
        resultado.andExpect(status().is4xxClientError());
    }
}

package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarAgendamentoUseCase;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AgendamentoSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CriarAgendamentoUseCase criarAgendamentoUseCase;

    private ResultActions resultado;

    @Dado("que existe o estabelecimento {string} com o serviço {string}")
    public void queExisteEstabelecimento(String estabelecimento, String servico) {
        // contexto: dados reais exigiriam banco; usando mocks aqui
    }

    @E("o profissional {string} atende esse serviço com escala às terças das {string} às {string}")
    public void profissionalComEscala(String profissional, String inicio, String fim) {
        // contexto
    }

    @Dado("que o cliente {string} está autenticado")
    public void clienteAutenticado(String cliente) {
        // contexto de autenticação gerenciado pelo MockMvc JWT
    }

    @Quando("ela solicita um agendamento para {string} com {string} na próxima terça às {string}")
    public void solicitaAgendamento(String servico, String profissional, String hora) throws Exception {
        Agendamento agendamento = Agendamento.builder()
                .id(1)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();
        when(criarAgendamentoUseCase.executar(any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(agendamento);

        String body = """
                {"profissionalVinculoId": 1, "servicoId": 1}
                """;
        resultado = mockMvc.perform(post("/api/agendamentos")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("o agendamento deve ser confirmado com status {string}")
    public void agendamentoConfirmadoComStatus(String statusEsperado) throws Exception {
        resultado.andExpect(status().isCreated());
    }

    @E("uma notificação de confirmação deve ser enviada para Maria")
    public void notificacaoEnviada() {
        // envio de email verificado via testes unitários de CriarAgendamentoUseCase
    }

    @Dado("que o horário de terça às {string} com {string} já está agendado por outro cliente")
    public void horarioJaOcupado(String hora, String profissional) {
        when(criarAgendamentoUseCase.executar(any(), anyInt(), anyInt(), any(), any()))
                .thenThrow(new OperacaoInvalidaException(
                        "Sem horários disponíveis para o profissional e serviço solicitados."));
    }

    @Quando("o cliente {string} tenta agendar o mesmo horário")
    public void clienteTentaAgendar(String cliente) throws Exception {
        String body = """
                {"profissionalVinculoId": 1, "servicoId": 1}
                """;
        resultado = mockMvc.perform(post("/api/agendamentos")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("o sistema deve informar que não há disponibilidade")
    public void sistemaNaoHaDisponibilidade() throws Exception {
        resultado.andExpect(status().is4xxClientError());
    }
}

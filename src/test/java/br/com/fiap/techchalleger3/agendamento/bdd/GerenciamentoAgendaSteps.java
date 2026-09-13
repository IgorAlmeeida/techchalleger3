package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendaUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GerenciamentoAgendaSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CancelarAgendaUseCase cancelarAgendaUseCase;

    private ResultActions resultado;

    @Dado("que existe uma agenda de id {int} com agendamentos confirmados")
    public void agendaExisteComAgendamentos(int agendaId) {
        doNothing().when(cancelarAgendaUseCase).executar(eq(agendaId), anyString(), anyBoolean());
    }

    @Dado("que o admin está autenticado como ADMIN")
    public void adminAutenticado() {
        // autenticação via jwt() aplicada nos passos Quando
    }

    @Quando("o estabelecimento cancela a agenda de id {int}")
    public void cancelaAgenda(int agendaId) throws Exception {
        resultado = mockMvc.perform(patch("/api/agendas/" + agendaId + "/cancelar")
                .with(jwt()
                        .jwt(builder -> builder.claim("sub", "admin-sub-uuid"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Entao("a agenda deve ser cancelada com sucesso")
    public void agendaCancelada() throws Exception {
        resultado.andExpect(status().isNoContent());
    }

    @Dado("que um profissional não autorizado está autenticado como PROFISSIONAL")
    public void profissionalNaoAutorizado() {
        // configurado via mock no Quando
    }

    @Quando("esse profissional tenta cancelar a agenda de id {int}")
    public void profissionalTentaCancelarAgenda(int agendaId) throws Exception {
        doThrow(new AcessoNegadoException("Profissional não autorizado a cancelar esta agenda."))
                .when(cancelarAgendaUseCase).executar(eq(agendaId), anyString(), anyBoolean());

        resultado = mockMvc.perform(patch("/api/agendas/" + agendaId + "/cancelar")
                .with(jwt()
                        .jwt(builder -> builder.claim("sub", "profissional-sub-uuid"))
                        .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
    }

    @Entao("o sistema deve negar o acesso ao cancelamento da agenda")
    public void acessoNegadoAoCancelamento() throws Exception {
        resultado.andExpect(status().is4xxClientError());
    }
}

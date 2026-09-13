package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.ConfirmarPresencaUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConfirmacaoPresencaSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfirmarPresencaUseCase confirmarPresencaUseCase;

    private ResultActions resultado;

    @Dado("que existe um agendamento ativo de id {int} com status {string}")
    public void agendamentoAtivoExiste(int agendamentoId, String status) {
        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .presencaConfirmada(true)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();
        when(confirmarPresencaUseCase.executar(eq(agendamentoId))).thenReturn(agendamento);
    }

    @Dado("que o profissional está autenticado como PROFISSIONAL")
    public void profissionalAutenticado() {
        // autenticação via jwt() aplicada nos passos Quando
    }

    @Quando("o estabelecimento confirma a presença no agendamento de id {int}")
    public void confirmaPresenca(int agendamentoId) throws Exception {
        resultado = mockMvc.perform(patch("/api/agendamentos/" + agendamentoId + "/confirmar-presenca")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
    }

    @Entao("a confirmação de presença deve ser registrada com sucesso")
    public void presencaRegistrada() throws Exception {
        resultado.andExpect(status().isOk());
    }

    @Quando("o estabelecimento tenta confirmar a presença no agendamento de id {int}")
    public void tentaConfirmarPresencaInexistente(int agendamentoId) throws Exception {
        when(confirmarPresencaUseCase.executar(eq(agendamentoId)))
                .thenThrow(new RegistroNaoEncontradoException("Agendamento", agendamentoId));

        resultado = mockMvc.perform(patch("/api/agendamentos/" + agendamentoId + "/confirmar-presenca")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
    }

    @Entao("o sistema deve informar que o agendamento não foi encontrado")
    public void agendamentoNaoEncontrado() throws Exception {
        resultado.andExpect(status().isNotFound());
    }
}

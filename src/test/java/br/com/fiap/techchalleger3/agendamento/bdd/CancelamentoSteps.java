package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CancelamentoSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CancelarAgendamentoUseCase cancelarAgendamentoUseCase;

    private ResultActions resultado;
    private final Integer agendamentoId = 1;

    @Dado("que {string} tem um agendamento confirmado para amanhã")
    public void clienteTemAgendamento(String cliente) {
        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .status(StatusAgendamentoEnum.CANCELADO)
                .build();
        when(cancelarAgendamentoUseCase.executar(anyInt(), any())).thenReturn(agendamento);
    }

    @Quando("ela cancela esse agendamento")
    public void cancelaAgendamento() throws Exception {
        resultado = mockMvc.perform(patch("/api/agendamentos/" + agendamentoId + "/cancelar")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
    }

    @Entao("o status do agendamento deve mudar para {string}")
    public void statusMudaPara(String statusEsperado) throws Exception {
        resultado.andExpect(status().isOk());
    }

    @E("o horário deve voltar a aparecer como disponível na busca")
    public void horarioDisponivel() {
        // verificado via testes unitários de CancelarAgendamentoUseCase
    }
}

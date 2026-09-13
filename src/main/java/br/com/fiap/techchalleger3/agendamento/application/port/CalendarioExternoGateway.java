package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;

public interface CalendarioExternoGateway {
    String criarEvento(Agendamento agendamento, IntegracaoCalendarioExterno integracao);
    void removerEvento(String googleEventId, IntegracaoCalendarioExterno integracao);
}

package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;

public interface CalendarioExportPort {
    byte[] exportarIcs(Agendamento agendamento);
}

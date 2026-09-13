package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;

/**
 * Porta de saída para exportação de agendamentos em formato iCalendar (.ics) para importação em agendas externas.
 */
public interface CalendarioExportPort {
    byte[] exportarIcs(Agendamento agendamento);
}

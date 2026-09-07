package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado da tentativa de agendamento: confirmado ou sem disponibilidade")
public record AgendamentoOuFilaResponse(
        @Schema(description = "Discriminador do resultado: AGENDAMENTO quando slot foi reservado, SEM_VAGA quando não havia vaga",
                example = "AGENDAMENTO", allowableValues = {"AGENDAMENTO", "SEM_VAGA"})
        String tipo,

        @Schema(description = "Dados do agendamento criado (preenchido quando tipo = AGENDAMENTO, null caso contrário)")
        AgendamentoResponse agendamento
) {
    public static AgendamentoOuFilaResponse deAgendamento(AgendamentoResponse agendamento) {
        return new AgendamentoOuFilaResponse("AGENDAMENTO", agendamento);
    }

    public static AgendamentoOuFilaResponse semVaga() {
        return new AgendamentoOuFilaResponse("SEM_VAGA", null);
    }
}

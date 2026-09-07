package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Dados de um slot de agendamento")
public record AgendamentoResponse(
        @Schema(description = "Identificador único do agendamento", example = "42")
        Integer id,

        @Schema(description = "Identificador da agenda à qual este slot pertence", example = "10")
        Integer agendaId,

        @Schema(description = "Data da agenda deste slot", example = "2026-08-05")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,

        @Schema(description = "Serviço deste slot (null se DISPONIVEL)")
        ServicoResumo servico,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Cliente que ocupa este slot (null se DISPONIVEL)")
        ClienteResumo cliente,

        @Schema(description = "Status atual do slot", example = "AGENDADO",
                allowableValues = {"DISPONIVEL", "AGENDADO", "CANCELADO", "REALIZADO"})
        StatusAgendamentoEnum status,

        @Schema(description = "Indica se a presença do cliente foi confirmada na recepção", example = "false")
        Boolean presencaConfirmada
) {}

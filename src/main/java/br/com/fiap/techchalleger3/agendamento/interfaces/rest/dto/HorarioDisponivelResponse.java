package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Slot disponível para agendamento")
public record HorarioDisponivelResponse(

        @Schema(description = "Identificador da agenda à qual este slot pertence", example = "10")
        Integer agendaId,

        @Schema(description = "Identificador do slot disponível — use para reservar diretamente via POST /api/agendamentos", example = "42")
        Integer agendamentoId,

        @Schema(description = "Data do atendimento", example = "2026-09-20")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,

        @Schema(description = "Horário de início", example = "09:00:00")
        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaInicio,

        @Schema(description = "Horário de fim", example = "09:30:00")
        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaFim,

        @Schema(description = "Profissional que atende neste slot")
        ProfissionalResumo profissional,

        @Schema(description = "Estabelecimento onde ocorre o atendimento")
        EstabelecimentoResumo estabelecimento
) {}

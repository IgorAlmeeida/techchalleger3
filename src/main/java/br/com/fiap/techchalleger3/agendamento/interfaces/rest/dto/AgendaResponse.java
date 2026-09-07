package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Resumo de uma agenda gerada a partir de uma escala")
public record AgendaResponse(
        @Schema(description = "Identificador único da agenda", example = "10")
        Integer id,

        @Schema(description = "Identificador da escala que originou esta agenda", example = "5")
        Integer escalaId,

        @Schema(description = "Data concreta desta agenda", example = "2026-08-05")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,

        @Schema(description = "Dia da semana desta agenda", example = "TERCA")
        DiaSemanaEnum diaSemana,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Vínculo profissional/estabelecimento responsável por esta agenda")
        ProfissionalVinculoResumo profissionalVinculo
) {}

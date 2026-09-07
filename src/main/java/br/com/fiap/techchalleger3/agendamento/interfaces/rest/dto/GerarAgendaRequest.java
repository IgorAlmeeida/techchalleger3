package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Parâmetros para geração de agendas concretas a partir de uma escala recorrente")
public record GerarAgendaRequest(
        @Schema(description = "Identificador da escala recorrente a usar como base", example = "5")
        @NotNull Integer escalaId,

        @Schema(description = "Data de início do intervalo de geração (inclusive)", example = "2026-08-01")
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,

        @Schema(description = "Data de fim do intervalo de geração (inclusive)", example = "2026-08-31")
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
) {}

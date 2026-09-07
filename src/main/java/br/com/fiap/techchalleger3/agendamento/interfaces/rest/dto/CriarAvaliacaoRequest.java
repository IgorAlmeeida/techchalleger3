package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criação de uma avaliação de atendimento")
public record CriarAvaliacaoRequest(
        @Schema(description = "Identificador do agendamento avaliado")
        @NotNull Integer agendamentoId,

        @Schema(description = "Nota de 1 a 5", example = "5")
        @Min(1) @Max(5) int nota,

        @Schema(description = "Comentário opcional sobre o atendimento", example = "Excelente atendimento!")
        String comentario
) {}

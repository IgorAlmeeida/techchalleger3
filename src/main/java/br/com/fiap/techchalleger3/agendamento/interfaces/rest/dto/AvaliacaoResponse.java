package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados de uma avaliação de atendimento")
public record AvaliacaoResponse(
        @Schema(description = "Identificador único da avaliação")
        Integer id,

        @Schema(description = "Identificador do agendamento avaliado")
        Integer agendamentoId,

        @Schema(description = "Identificador do cliente que realizou a avaliação")
        Integer clienteId,

        @Schema(description = "Identificador do estabelecimento avaliado")
        Integer estabelecimentoId,

        @Schema(description = "Identificador do vínculo profissional avaliado")
        Integer profissionalVinculoId,

        @Schema(description = "Nota de 1 a 5", example = "5")
        int nota,

        @Schema(description = "Comentário sobre o atendimento", example = "Excelente atendimento!")
        String comentario,

        @Schema(description = "Data/hora da avaliação")
        LocalDateTime dhInsert
) {}

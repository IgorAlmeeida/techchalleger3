package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de um serviço dentro de uma agenda ou escala")
public record AgendaItemResumoResponse(
        @Schema(description = "Identificador do serviço", example = "2")
        Integer servicoId,

        @Schema(description = "Nome do serviço", example = "Consulta Clínico Geral")
        String nome,

        @Schema(description = "Duração do serviço em minutos", example = "30")
        Integer duracaoMinutos
) {}

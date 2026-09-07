package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados de um serviço")
public record ServicoResponse(
        @Schema(description = "Identificador único do serviço", example = "1")
        Integer id,

        @Schema(description = "Nome do serviço", example = "Consulta Clínico Geral")
        String nome,

        @Schema(description = "Duração do serviço em minutos", example = "30")
        Integer duracaoMinutos,

        @Schema(description = "Preço do serviço", example = "150.00")
        BigDecimal preco,

        @Schema(description = "Indica se o serviço está ativo no sistema", example = "true")
        Boolean ativo,

        @Schema(description = "Data/hora de criação do registro", example = "2026-01-15T10:30:00")
        LocalDateTime dhInsert,

        @Schema(description = "Data/hora da última atualização", example = "2026-06-01T14:00:00")
        LocalDateTime dhAtualizacao
) {}

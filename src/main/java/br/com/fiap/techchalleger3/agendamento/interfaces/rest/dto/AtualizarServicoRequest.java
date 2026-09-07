package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Dados para atualização de um serviço")
public record AtualizarServicoRequest(
        @Schema(description = "Nome do serviço", example = "Consulta Clínico Geral")
        @NotBlank String nome,

        @Schema(description = "Duração do serviço em minutos (múltiplo de 5)", example = "30")
        @NotNull @Positive Integer duracaoMinutos,

        @Schema(description = "Preço do serviço", example = "150.00")
        BigDecimal preco
) {}

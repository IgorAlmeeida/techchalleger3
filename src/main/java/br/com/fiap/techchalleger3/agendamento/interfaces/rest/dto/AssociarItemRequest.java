package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para associar um serviço a um vínculo de profissional")
public record AssociarItemRequest(
        @Schema(description = "Identificador do serviço a ser associado", example = "2")
        @NotNull Integer servicoId
) {}

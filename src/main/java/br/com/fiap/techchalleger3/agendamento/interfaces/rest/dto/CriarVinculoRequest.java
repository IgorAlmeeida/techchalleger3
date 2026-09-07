package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados para vincular um profissional a um estabelecimento")
public record CriarVinculoRequest(
        @Schema(description = "Identificador do profissional", example = "3")
        @NotNull Integer profissionalId,

        @Schema(description = "Identificador do estabelecimento", example = "1")
        @NotNull Integer estabelecimentoId,

        @Schema(description = "Data de início do vínculo (inclusive)", example = "2026-08-01")
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio
) {}

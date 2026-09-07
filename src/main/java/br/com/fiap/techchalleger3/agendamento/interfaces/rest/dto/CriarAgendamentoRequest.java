package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados para criação de um agendamento pelo cliente")
public record CriarAgendamentoRequest(
        @Schema(description = "Identificador do vínculo profissional/estabelecimento desejado", example = "1")
        @NotNull Integer profissionalVinculoId,

        @Schema(description = "Identificador do serviço desejado", example = "2")
        @NotNull Integer servicoId,

        @Schema(description = "Data preferencial para o agendamento (busca o primeiro slot disponível a partir desta data)", example = "2026-08-01")
        LocalDate dataPreferencia,

        @Schema(description = "Id de um slot de agendamento específico (DISPONIVEL) para reserva direta. Opcional — se omitido, o sistema busca automaticamente.", example = "42")
        Integer agendamentoId
) {}

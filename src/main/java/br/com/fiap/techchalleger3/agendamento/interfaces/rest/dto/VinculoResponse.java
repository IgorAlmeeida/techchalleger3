package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de um vínculo entre profissional e estabelecimento")
public record VinculoResponse(
        @Schema(description = "Identificador único do vínculo", example = "1")
        Integer id,

        @Schema(description = "Profissional vinculado")
        ProfissionalResumo profissional,

        @Schema(description = "Estabelecimento vinculado")
        EstabelecimentoResumo estabelecimento,

        @Schema(description = "Data de início do vínculo", example = "2026-08-01")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,

        @Schema(description = "Data de encerramento do vínculo (null se ainda ativo)", example = "2026-12-31")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
) {}

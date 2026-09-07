package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Parâmetros de busca de estabelecimentos")
public record BuscarEstabelecimentosRequest(
        @Schema(description = "Filtrar por nome (busca parcial)", example = "Clínica")
        String nome,

        @Schema(description = "Filtrar por localização", example = "São Paulo")
        String localizacao,

        @Schema(description = "Filtrar por serviço oferecido", example = "Consulta")
        String servico,

        @Schema(description = "Preço mínimo do serviço", example = "50.00")
        BigDecimal precoMin,

        @Schema(description = "Preço máximo do serviço", example = "200.00")
        BigDecimal precoMax,

        @Schema(description = "Nota mínima do estabelecimento (0.0 a 5.0)", example = "4.0")
        Double notaMinima,

        @Schema(description = "Data para verificar disponibilidade", example = "2026-08-15")
        LocalDate data
) {}

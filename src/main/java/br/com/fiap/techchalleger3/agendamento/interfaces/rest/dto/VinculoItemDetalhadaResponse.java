package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serviço associado a um vínculo, com detalhes")
public record VinculoItemDetalhadaResponse(
        @Schema(description = "Identificador único da associação vínculo/serviço", example = "10")
        Integer id,

        @Schema(description = "Vínculo profissional/estabelecimento desta associação")
        ProfissionalVinculoResumo profissionalVinculo,

        @Schema(description = "Serviço associado")
        ServicoResumo servico
) {}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Profissional disponível em um estabelecimento, com seus serviços disponíveis para agendamento")
public record ProfissionalDisponivelResponse(
        @Schema(description = "Vínculo do profissional com o estabelecimento")
        ProfissionalVinculoResumo profissionalVinculo,

        @Schema(description = "Serviços que o profissional oferece neste estabelecimento")
        List<ServicoResumo> servicos
) {}

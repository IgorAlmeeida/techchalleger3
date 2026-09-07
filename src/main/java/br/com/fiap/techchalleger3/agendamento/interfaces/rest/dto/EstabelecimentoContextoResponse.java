package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estabelecimento disponível para o usuário autenticado selecionar como contexto de trabalho")
public record EstabelecimentoContextoResponse(
        @Schema(description = "Identificador único do estabelecimento", example = "1")
        Integer id,

        @Schema(description = "Nome do estabelecimento", example = "Clínica Saúde Total")
        String nome,

        @Schema(description = "CNPJ do estabelecimento", example = "12345678000195")
        String cnpj,

        @Schema(description = "Endereço do estabelecimento", example = "Rua da Saúde, 500, Curitiba - PR")
        String endereco
) {}

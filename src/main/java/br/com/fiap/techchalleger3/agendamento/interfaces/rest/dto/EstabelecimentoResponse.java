package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Dados de um estabelecimento")
public record EstabelecimentoResponse(
        @Schema(description = "Identificador único do estabelecimento", example = "1")
        Integer id,

        @Schema(description = "Nome do estabelecimento", example = "Clínica Saúde Total")
        String nome,

        @Schema(description = "CNPJ do estabelecimento (somente dígitos)", example = "12345678000195")
        String cnpj,

        @Schema(description = "Endereço completo", example = "Rua da Saúde, 500, Curitiba - PR")
        String endereco,

        @Schema(description = "Telefone de contato", example = "(11) 3456-7890")
        String telefone,

        @Schema(description = "Nome completo do responsável", example = "Carlos Mendes")
        String responsavelNome,

        @Schema(description = "CPF do responsável (somente dígitos)", example = "98765432100")
        String responsavelCpf,

        @Schema(description = "URLs de fotos do estabelecimento")
        List<String> fotosUrls,

        @Schema(description = "Indica se o estabelecimento está ativo no sistema", example = "true")
        Boolean ativo,

        @Schema(description = "Data/hora de criação do registro", example = "2026-01-15T10:30:00")
        LocalDateTime dhInsert,

        @Schema(description = "Data/hora da última atualização", example = "2026-06-01T14:00:00")
        LocalDateTime dhAtualizacao
) {}

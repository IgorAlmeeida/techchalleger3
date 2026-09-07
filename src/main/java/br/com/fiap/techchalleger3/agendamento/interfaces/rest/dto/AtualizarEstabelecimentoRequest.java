package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "Dados para atualização de um estabelecimento")
public record AtualizarEstabelecimentoRequest(
        @Schema(description = "Nome do estabelecimento", example = "Clínica Saúde Total")
        @NotBlank String nome,

        @Schema(description = "CNPJ do estabelecimento (somente dígitos)", example = "12345678000195")
        String cnpj,

        @Schema(description = "Endereço completo do estabelecimento", example = "Rua da Saúde, 500, Curitiba - PR")
        String endereco,

        @Schema(description = "Telefone de contato", example = "(11) 3456-7890")
        String telefone,

        @Schema(description = "Nome completo do responsável", example = "Carlos Mendes")
        String responsavelNome,

        @Schema(description = "CPF do responsável (somente dígitos)", example = "98765432100")
        String responsavelCpf,

        @Schema(description = "URLs de fotos do estabelecimento")
        List<String> fotosUrls
) {}

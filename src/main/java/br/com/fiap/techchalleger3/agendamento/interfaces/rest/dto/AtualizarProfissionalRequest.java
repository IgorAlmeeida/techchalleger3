package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "Dados para atualização de um profissional")
public record AtualizarProfissionalRequest(
        @Schema(description = "Nome completo do profissional", example = "Dr. Carlos Silva")
        @NotBlank String nome,

        @Schema(description = "Especialidades do profissional", example = "[\"Cardiologia\"]")
        List<String> especialidades,

        @Schema(description = "Endereço completo do profissional")
        String endereco
) {}

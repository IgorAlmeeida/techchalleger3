package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Dados de um profissional")
public record ProfissionalResponse(
        @Schema(description = "Identificador do profissional", example = "7")
        Integer id,

        @Schema(description = "Nome completo do profissional", example = "Dr. Carlos Silva")
        String nome,

        @Schema(description = "Especialidades do profissional", example = "[\"Cardiologia\"]")
        List<String> especialidades,

        @Schema(description = "Endereço do profissional")
        String endereco,

        @Schema(description = "Indica se o profissional está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Data/hora de cadastro")
        LocalDateTime dhInsert,

        @Schema(description = "Data/hora da última atualização")
        LocalDateTime dhAtualizacao
) {}

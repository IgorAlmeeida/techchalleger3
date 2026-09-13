package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

        @Schema(description = "Data/hora de cadastro", example = "2026-01-15 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dhInsert,

        @Schema(description = "Data/hora da última atualização", example = "2026-06-01 14:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dhAtualizacao
) {}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "Dados para cadastro de profissional pelo administrador")
public record CadastrarProfissionalRequest(
        @Schema(description = "Nome completo do profissional", example = "Dra. Ana Lima")
        @NotBlank String nome,

        @Schema(description = "E-mail do profissional, usado como login no Keycloak", example = "ana.lima@clinica.com")
        @NotBlank @Email String email,

        @Schema(description = "Especialidades do profissional", example = "[\"Clínico Geral\",\"Pediatria\"]")
        List<String> especialidades,

        @Schema(description = "Endereço completo do profissional", example = "Av. Paulista, 1000, São Paulo - SP")
        String endereco
) {}

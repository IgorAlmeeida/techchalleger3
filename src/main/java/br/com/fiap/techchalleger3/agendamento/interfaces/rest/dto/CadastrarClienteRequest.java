package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados para auto-cadastro de cliente")
public record CadastrarClienteRequest(
        @Schema(description = "Nome completo do cliente", example = "João da Silva")
        @NotBlank String nome,

        @Schema(description = "E-mail do cliente, usado como login no Keycloak", example = "joao.silva@email.com")
        @NotBlank @Email String email,

        @Schema(description = "Senha de acesso (mínimo 8 caracteres, deve conter letras e números)", example = "Senha@123")
        @NotBlank String password,

        @Schema(description = "CPF do cliente (somente dígitos)", example = "12345678901")
        @NotBlank String cpf,

        @Schema(description = "Data de nascimento do cliente", example = "1990-05-15")
        @NotNull LocalDate dataNascimento,

        @Schema(description = "Telefone de contato do cliente", example = "(11) 91234-5678")
        String telefone,

        @Schema(description = "Sexo do cliente (M, F ou outro)", example = "M")
        String sexo,

        @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123, São Paulo - SP")
        String endereco
) {}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequest(
        @Schema(description = "Nome de usuário ou e-mail cadastrado no Keycloak", example = "joao.silva@email.com")
        @NotBlank String username,

        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank String password
) {}

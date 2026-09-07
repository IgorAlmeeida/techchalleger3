package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token para renovação do access token")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token JWT obtido no último login ou refresh", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank String refreshToken
) {}

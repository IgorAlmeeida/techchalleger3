package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tokens JWT retornados após autenticação bem-sucedida")
public record LoginResponse(
        @Schema(description = "Access token JWT para uso nas chamadas autenticadas", example = "eyJhbGciOiJSUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tempo de expiração do access token em segundos", example = "300")
        Integer expiresIn,

        @Schema(description = "Tipo do token (sempre Bearer)", example = "Bearer")
        String tokenType,

        @Schema(description = "Refresh token para renovação do access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "Tempo de expiração do refresh token em segundos", example = "1800")
        Integer refreshExpiresIn
) {}

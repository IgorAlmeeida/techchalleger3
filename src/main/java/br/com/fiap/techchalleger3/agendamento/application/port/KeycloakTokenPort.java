package br.com.fiap.techchalleger3.agendamento.application.port;

public interface KeycloakTokenPort {
    TokenResponse obterToken(String username, String password);
    TokenResponse renovarToken(String refreshToken);

    record TokenResponse(String accessToken, Integer expiresIn, String tokenType,
                         String refreshToken, Integer refreshExpiresIn) {}
}

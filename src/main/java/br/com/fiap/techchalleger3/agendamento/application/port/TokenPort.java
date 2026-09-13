package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;

public interface TokenPort {

    record TokenResponse(String accessToken, Integer expiresIn, String tokenType,
                         String refreshToken, Integer refreshExpiresIn) {}

    TokenResponse gerarTokens(Usuario usuario);

    String gerarRefreshToken(String uuid);

    String validarRefreshToken(String refreshToken);
}

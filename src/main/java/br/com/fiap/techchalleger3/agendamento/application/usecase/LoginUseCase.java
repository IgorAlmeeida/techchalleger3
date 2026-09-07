package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final KeycloakTokenPort keycloakTokenPort;

    public KeycloakTokenPort.TokenResponse executar(String username, String password) {
        return keycloakTokenPort.obterToken(username, password);
    }
}

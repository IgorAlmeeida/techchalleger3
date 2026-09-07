package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRenovarTokenUseCaseTest {

    @Mock private KeycloakTokenPort keycloakTokenPort;
    @InjectMocks private LoginUseCase loginUseCase;
    @InjectMocks private RenovarTokenUseCase renovarTokenUseCase;

    @Test
    void login_delegaParaPort() {
        KeycloakTokenPort.TokenResponse resp = mock(KeycloakTokenPort.TokenResponse.class);
        when(keycloakTokenPort.obterToken("user", "pass")).thenReturn(resp);

        KeycloakTokenPort.TokenResponse result = loginUseCase.executar("user", "pass");

        assertThat(result).isSameAs(resp);
    }

    @Test
    void renovar_delegaParaPort() {
        KeycloakTokenPort.TokenResponse resp = mock(KeycloakTokenPort.TokenResponse.class);
        when(keycloakTokenPort.renovarToken("rt")).thenReturn(resp);

        KeycloakTokenPort.TokenResponse result = renovarTokenUseCase.executar("rt");

        assertThat(result).isSameAs(resp);
    }
}

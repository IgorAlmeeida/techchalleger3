package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.TokenPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRenovarTokenUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private PasswordPort passwordPort;
    @Mock private TokenPort tokenPort;

    @InjectMocks private LoginUseCase loginUseCase;

    @Test
    void login_retornaTokens_quandoCredenciaisValidas() {
        Usuario usuario = Usuario.builder().id(1).email("u@x.com").senhaHash("hash").build();
        TokenPort.TokenResponse resp = new TokenPort.TokenResponse("acc", 300, "Bearer", "ref", 1800);

        when(usuarioPort.buscarPorEmail("u@x.com")).thenReturn(Optional.of(usuario));
        when(passwordPort.matches("pass", "hash")).thenReturn(true);
        when(tokenPort.gerarTokens(usuario)).thenReturn(resp);

        TokenPort.TokenResponse result = loginUseCase.executar("u@x.com", "pass");

        assertThat(result.accessToken()).isEqualTo("acc");
    }

    @Test
    void login_lancaExcecao_quandoEmailNaoEncontrado() {
        when(usuarioPort.buscarPorEmail("nope@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.executar("nope@x.com", "pass"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_lancaExcecao_quandoSenhaErrada() {
        Usuario usuario = Usuario.builder().id(1).email("u@x.com").senhaHash("hash").build();
        when(usuarioPort.buscarPorEmail("u@x.com")).thenReturn(Optional.of(usuario));
        when(passwordPort.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.executar("u@x.com", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}

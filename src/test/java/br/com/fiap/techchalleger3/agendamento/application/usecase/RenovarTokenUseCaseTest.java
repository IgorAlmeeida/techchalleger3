package br.com.fiap.techchalleger3.agendamento.application.usecase;

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
class RenovarTokenUseCaseTest {

    @Mock private TokenPort tokenPort;
    @Mock private UsuarioRepositoryPort usuarioPort;

    @InjectMocks private RenovarTokenUseCase useCase;

    @Test
    void executar_comRefreshTokenValido_geraNovosTokens() {
        Usuario usuario = Usuario.builder().id(1).uuid("uuid-123").build();
        TokenPort.TokenResponse novaResposta = new TokenPort.TokenResponse("novo-acc", 1800, "Bearer", "novo-ref", 604800);

        when(tokenPort.validarRefreshToken("refresh-valido")).thenReturn("uuid-123");
        when(usuarioPort.buscarPorUuid("uuid-123")).thenReturn(Optional.of(usuario));
        when(tokenPort.gerarTokens(usuario)).thenReturn(novaResposta);

        TokenPort.TokenResponse resultado = useCase.executar("refresh-valido");

        assertThat(resultado.accessToken()).isEqualTo("novo-acc");
        assertThat(resultado.refreshToken()).isEqualTo("novo-ref");
    }

    @Test
    void executar_comRefreshTokenInvalido_propagaExcecaoDoTokenPort() {
        when(tokenPort.validarRefreshToken("refresh-invalido"))
                .thenThrow(new CredenciaisInvalidasException());

        assertThatThrownBy(() -> useCase.executar("refresh-invalido"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void executar_comUsuarioNaoEncontrado_lancaCredenciaisInvalidas() {
        when(tokenPort.validarRefreshToken("refresh-valido")).thenReturn("uuid-fantasma");
        when(usuarioPort.buscarPorUuid("uuid-fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("refresh-valido"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterarSenhaUseCaseTest {

    @Mock private KeycloakTokenPort keycloakTokenPort;
    @Mock private KeycloakAdminPort keycloakAdminPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @InjectMocks private AlterarSenhaUseCase useCase;

    @Test
    void deveAlterarSenha_quandoCredenciaisValidas() {
        Usuario usuario = Usuario.builder().id(1).keycloakId("kc-1").build();
        when(usuarioPort.buscarPorCodKeycloak("sub-1")).thenReturn(Optional.of(usuario));

        useCase.executar("sub-1", "email@test.com", "senhaAtual", "novaSenha123A");

        verify(keycloakTokenPort).obterToken("email@test.com", "senhaAtual");
        verify(keycloakAdminPort).redefinirSenha("kc-1", "novaSenha123A", false);
    }

    @Test
    void deveLancar_quandoCredenciaisInvalidas() {
        doThrow(new CredenciaisInvalidasException()).when(keycloakTokenPort).obterToken("email@test.com", "errada");

        assertThatThrownBy(() -> useCase.executar("sub-1", "email@test.com", "errada", "nova"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveLancar_quandoUsuarioNaoEncontrado() {
        when(usuarioPort.buscarPorCodKeycloak("sub-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("sub-x", "email@test.com", "senha", "nova"))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }
}

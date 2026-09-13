package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterarSenhaUseCaseTest {

    @Mock private PasswordPort passwordPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @InjectMocks private AlterarSenhaUseCase useCase;

    @Test
    void deveAlterarSenha_quandoCredenciaisValidas() {
        Usuario usuario = Usuario.builder().id(1).uuid("uid-1").senhaHash("hash-atual").build();
        when(usuarioPort.buscarPorUuid("uid-1")).thenReturn(Optional.of(usuario));
        when(passwordPort.matches("senhaAtual", "hash-atual")).thenReturn(true);
        when(passwordPort.encode("novaSenha123A")).thenReturn("novo-hash");

        useCase.executar("uid-1", "senhaAtual", "novaSenha123A");

        verify(usuarioPort).atualizarSenha("uid-1", "novo-hash");
    }

    @Test
    void deveLancar_quandoSenhaAtualIncorreta() {
        Usuario usuario = Usuario.builder().id(1).uuid("uid-1").senhaHash("hash-atual").build();
        when(usuarioPort.buscarPorUuid("uid-1")).thenReturn(Optional.of(usuario));
        when(passwordPort.matches("errada", "hash-atual")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar("uid-1", "errada", "nova"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveLancar_quandoUsuarioNaoEncontrado() {
        when(usuarioPort.buscarPorUuid("uid-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("uid-x", "senha", "nova"))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }
}

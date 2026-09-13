package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedefinirSenhaEsquecidaUseCaseTest {

    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private PasswordPort passwordPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private RedefinirSenhaEsquecidaUseCase useCase;

    @Test
    void deveEnviarEmail_quandoClienteEncontrado() {
        Usuario usuario = Usuario.builder().id(10).uuid("uid-1").nome("Maria").build();
        Cliente cliente = Cliente.builder().id(1).nome("Maria").usuarioId(10).build();
        when(usuarioPort.buscarPorEmail("maria@test.com")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorEmail("maria@test.com")).thenReturn(Optional.of(cliente));
        when(passwordPort.encode(anyString())).thenReturn("novo-hash");

        useCase.executar("maria@test.com");

        verify(usuarioPort).atualizarSenha("uid-1", "novo-hash");
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void deveEnviarEmail_quandoProfissionalEncontrado() {
        Usuario usuario = Usuario.builder().id(20).uuid("uid-2").nome("Dr. João").build();
        Profissional profissional = Profissional.builder().id(1).nome("Dr. João").usuarioId(20).build();
        when(usuarioPort.buscarPorEmail("joao@test.com")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorEmail("joao@test.com")).thenReturn(Optional.empty());
        when(profissionalPort.buscarPorEmail("joao@test.com")).thenReturn(Optional.of(profissional));
        when(passwordPort.encode(anyString())).thenReturn("novo-hash");

        useCase.executar("joao@test.com");

        verify(usuarioPort).atualizarSenha("uid-2", "novo-hash");
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void naoFazNada_quandoEmailNaoEncontrado() {
        when(usuarioPort.buscarPorEmail("inexistente@test.com")).thenReturn(Optional.empty());

        useCase.executar("inexistente@test.com");

        verify(usuarioPort, never()).atualizarSenha(any(), any());
        verify(emailSenderPort, never()).enviar(any());
    }
}

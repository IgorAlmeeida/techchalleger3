package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
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
    @Mock private KeycloakAdminPort keycloakAdminPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private RedefinirSenhaEsquecidaUseCase useCase;

    @Test
    void deveEnviarEmail_quandoClienteEncontrado() {
        Cliente cliente = Cliente.builder().id(1).nome("Maria").usuarioId(10).build();
        Usuario usuario = Usuario.builder().id(10).keycloakId("kc-1").build();
        when(clientePort.buscarPorEmail("maria@test.com")).thenReturn(Optional.of(cliente));
        when(usuarioPort.buscarPorId(10)).thenReturn(Optional.of(usuario));

        useCase.executar("maria@test.com");

        verify(keycloakAdminPort).redefinirSenha(anyString(), anyString(), any(Boolean.class));
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void deveEnviarEmail_quandoProfissionalEncontrado() {
        Profissional profissional = Profissional.builder().id(1).nome("Dr. João").usuarioId(20).build();
        Usuario usuario = Usuario.builder().id(20).keycloakId("kc-2").build();
        when(clientePort.buscarPorEmail("joao@test.com")).thenReturn(Optional.empty());
        when(profissionalPort.buscarPorEmail("joao@test.com")).thenReturn(Optional.of(profissional));
        when(usuarioPort.buscarPorId(20)).thenReturn(Optional.of(usuario));

        useCase.executar("joao@test.com");

        verify(keycloakAdminPort).redefinirSenha(anyString(), anyString(), any(Boolean.class));
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void naoFazNada_quandoEmailNaoEncontrado() {
        when(clientePort.buscarPorEmail("inexistente@test.com")).thenReturn(Optional.empty());
        when(profissionalPort.buscarPorEmail("inexistente@test.com")).thenReturn(Optional.empty());

        useCase.executar("inexistente@test.com");

        verify(keycloakAdminPort, never()).redefinirSenha(any(), any(), any(Boolean.class));
        verify(emailSenderPort, never()).enviar(any());
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SincronizarUsuarioUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @InjectMocks private SincronizarUsuarioUseCase useCase;

    @Test
    void deveRetornarExistente_quandoJaExiste() {
        Usuario existente = Usuario.builder().id(1).keycloakId("kc-1").role(RoleEnum.CLIENTE).build();
        when(usuarioPort.buscarPorCodKeycloak("kc-1")).thenReturn(Optional.of(existente));

        Usuario result = useCase.executar("kc-1", RoleEnum.CLIENTE, "Nome");

        assertThat(result).isSameAs(existente);
        verify(usuarioPort, never()).salvar(any());
    }

    @Test
    void deveCriarCliente_quandoNovo() {
        Usuario salvo = Usuario.builder().id(2).keycloakId("kc-2").role(RoleEnum.CLIENTE).build();
        when(usuarioPort.buscarPorCodKeycloak("kc-2")).thenReturn(Optional.empty());
        when(usuarioPort.salvar(any())).thenReturn(salvo);

        useCase.executar("kc-2", RoleEnum.CLIENTE, "Maria");

        verify(clientePort).salvar(any());
        verify(profissionalPort, never()).salvar(any());
    }

    @Test
    void deveCriarProfissional_quandoRoleProfissional() {
        Usuario salvo = Usuario.builder().id(3).keycloakId("kc-3").role(RoleEnum.PROFISSIONAL).build();
        when(usuarioPort.buscarPorCodKeycloak("kc-3")).thenReturn(Optional.empty());
        when(usuarioPort.salvar(any())).thenReturn(salvo);

        useCase.executar("kc-3", RoleEnum.PROFISSIONAL, "Dr. João");

        verify(profissionalPort).salvar(any());
        verify(clientePort, never()).salvar(any());
    }

    @Test
    void deveUsarKeycloakSubComoNome_quandoNomeNulo() {
        Usuario salvo = Usuario.builder().id(4).keycloakId("kc-4").role(RoleEnum.CLIENTE).build();
        when(usuarioPort.buscarPorCodKeycloak("kc-4")).thenReturn(Optional.empty());
        when(usuarioPort.salvar(any())).thenReturn(salvo);

        ArgumentCaptor<br.com.fiap.techchalleger3.agendamento.domain.model.Cliente> captor =
                ArgumentCaptor.forClass(br.com.fiap.techchalleger3.agendamento.domain.model.Cliente.class);

        useCase.executar("kc-4", RoleEnum.CLIENTE, null);

        verify(clientePort).salvar(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("kc-4");
    }
}

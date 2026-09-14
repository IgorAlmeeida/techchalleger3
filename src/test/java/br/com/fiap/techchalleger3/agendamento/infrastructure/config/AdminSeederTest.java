package br.com.fiap.techchalleger3.agendamento.infrastructure.config;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
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
class AdminSeederTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private PasswordPort passwordPort;
    @InjectMocks private AdminSeeder seeder;

    @Test
    void run_quandoAdminNaoExiste_criaUsuarioComHashEEmailCorretos() {
        when(usuarioPort.buscarPorEmail("admin@agendamento.com")).thenReturn(Optional.empty());
        when(passwordPort.encode("Admin@1234")).thenReturn("hash-simulado");

        seeder.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioPort).salvar(captor.capture());

        Usuario criado = captor.getValue();
        assertThat(criado.getEmail()).isEqualTo("admin@agendamento.com");
        assertThat(criado.getSenhaHash()).isEqualTo("hash-simulado");
        assertThat(criado.getRole()).isEqualTo(RoleEnum.ADMIN);
        assertThat(criado.getUuid()).isNotBlank();
    }

    @Test
    void run_quandoAdminJaExiste_naoCriaNovamente() {
        when(usuarioPort.buscarPorEmail("admin@agendamento.com"))
                .thenReturn(Optional.of(Usuario.builder().email("admin@agendamento.com").build()));

        seeder.run(null);

        verify(usuarioPort, never()).salvar(any());
    }
}

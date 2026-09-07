package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarProfissionalUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private KeycloakAdminPort keycloakAdminPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private CadastrarProfissionalUseCase useCase;

    @Test
    void deveCadastrar_eEnviarEmail() {
        when(keycloakAdminPort.criarUsuario(any(), any(), any(), any(), anyBoolean())).thenReturn("kc-1");
        when(usuarioPort.salvar(any())).thenReturn(Usuario.builder().id(1).build());
        Profissional prof = Profissional.builder().id(1).nome("Dr. João").build();
        when(profissionalPort.salvar(any())).thenReturn(prof);

        Profissional result = useCase.executar("Dr. João", "joao@test.com",
                List.of("Corte"), "Rua B");

        assertThat(result.getNome()).isEqualTo("Dr. João");
        verify(emailSenderPort).enviar(any());
    }
}

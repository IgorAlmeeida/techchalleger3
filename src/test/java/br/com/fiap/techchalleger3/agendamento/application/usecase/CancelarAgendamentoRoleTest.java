package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAgendamentoRoleTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private CancelarAgendamentoUseCase useCase;

    @Test
    void roleNula_lancaAcessoNegado() {
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-x").role(null).build();
        when(usuarioPort.buscarPorCodKeycloak("sub-x")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> useCase.executar(1, "sub-x"))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessageContaining("Role não autorizada");
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarAgendamentoUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EmailSenderPort emailSenderPort;

    @InjectMocks private CriarAgendamentoUseCase useCase;

    private Usuario usuario() {
        return Usuario.builder().id(10).keycloakId("sub-123").build();
    }

    private Cliente cliente() {
        return Cliente.builder().id(100).email("c@test.com").build();
    }

    private Servico servico() {
        return Servico.builder().id(2).nome("Corte").duracaoMinutos(30).build();
    }

    @Test
    void deveLancarExcecao_quandoUsuarioNaoEncontrado() {
        when(usuarioPort.buscarPorCodKeycloak("sub-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("sub-123", 1, 2, null, null))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoClienteNaoEncontrado() {
        when(usuarioPort.buscarPorCodKeycloak("sub-123")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("sub-123", 1, 2, null, null))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoAgendamentoJaExistente() {
        when(usuarioPort.buscarPorCodKeycloak("sub-123")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(
                Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(2)).thenReturn(Optional.of(servico()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(100, 1, 2)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("sub-123", 1, 2, null, null))
                .isInstanceOf(AgendamentoJaExistenteException.class);
    }

    @Test
    void deveLancarExcecao_quandoSemDisponibilidade() {
        when(usuarioPort.buscarPorCodKeycloak("sub-123")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(
                Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(2)).thenReturn(Optional.of(servico()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(100, 1, 2)).thenReturn(false);
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar("sub-123", 1, 2, null, null))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Sem horários disponíveis");
    }
}

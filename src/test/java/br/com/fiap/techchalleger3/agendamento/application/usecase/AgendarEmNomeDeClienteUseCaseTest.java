package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.EmailJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendarEmNomeDeClienteUseCaseTest {

    @Mock private ClienteRepositoryPort clientePort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private KeycloakAdminPort keycloakAdminPort;
    @Mock private EmailSenderPort emailSenderPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @InjectMocks private AgendarEmNomeDeClienteUseCase useCase;

    private Servico servico10min() {
        return Servico.builder().id(5).nome("Corte").duracaoMinutos(10).preco(BigDecimal.TEN).build();
    }

    @Test
    void lancaQuandoClienteJaPossuiAgendamentoAtivo() {
        Cliente cliente = Cliente.builder().id(10).build();
        when(clientePort.buscarPorCpf("123")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("123", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(AgendamentoJaExistenteException.class);
    }

    @Test
    void lancaQuandoSemHorariosDisponiveis() {
        Cliente cliente = Cliente.builder().id(10).build();
        when(clientePort.buscarPorCpf("123")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar("123", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void lancaQuandoCpfNaoExisteESemDadosObrigatorios() {
        when(clientePort.buscarPorCpf("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("999", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void lancaEmailJaCadastrado_quandoEmailDuplicado() {
        when(clientePort.buscarPorCpf("888")).thenReturn(Optional.empty());
        when(clientePort.buscarPorEmail("dup@x.com")).thenReturn(Optional.of(Cliente.builder().id(99).build()));

        assertThatThrownBy(() -> useCase.executar("888", "Novo", LocalDate.of(2000,1,1), null, null, null, "dup@x.com", 1, 5, null))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void lancaQuandoVinculoNaoExiste() {
        when(clientePort.buscarPorCpf("123")).thenReturn(Optional.of(Cliente.builder().id(10).build()));
        when(profissionalVinculoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("123", null, null, null, null, null, null, 99, 5, null))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }
}

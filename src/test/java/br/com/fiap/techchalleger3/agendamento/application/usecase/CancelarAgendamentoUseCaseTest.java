package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAgendamentoUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private EmailSenderPort emailSenderPort;

    @InjectMocks private CancelarAgendamentoUseCase useCase;

    private Usuario usuarioCliente() {
        return Usuario.builder().id(10).codKeycloak("sub-cliente").role(RoleEnum.CLIENTE).build();
    }

    private Cliente cliente(int id) {
        return Cliente.builder().id(id).email("cliente@test.com").build();
    }

    private Agendamento agendamentoPai(int agendaId, int clienteId, StatusAgendamentoEnum status) {
        return Agendamento.builder()
                .id(1)
                .agendaId(agendaId)
                .agendamentoPaiId(null)
                .clienteId(clienteId)
                .servicoId(5)
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(10, 0))
                .status(status)
                .presencaConfirmada(false)
                .build();
    }

    private Agenda agenda(int id) {
        return Agenda.builder()
                .id(id)
                .dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(20)
                .build();
    }

    @Test
    void deveCancelarComoCliente_quandoAgendamentoProprioEAgendado() {
        Usuario usuario = usuarioCliente();
        Cliente cli = cliente(100);
        Agendamento agendamento = agendamentoPai(7, 100, StatusAgendamentoEnum.AGENDADO);
        Agenda ag = agenda(7);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-cliente");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void deveLancarAcessoNegado_quandoClienteTentaCancelarAgendamentoDoutro() {
        Usuario usuario = usuarioCliente();
        Cliente cli = cliente(100);
        Agendamento agendamento = agendamentoPai(7, 999, StatusAgendamentoEnum.AGENDADO);

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar(1, "sub-cliente"))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void deveLancarOperacaoInvalida_quandoStatusNaoAgendado_comoCliente() {
        Usuario usuario = usuarioCliente();
        Cliente cli = cliente(100);
        Agendamento agendamento = agendamentoPai(7, 100, StatusAgendamentoEnum.DISPONIVEL);

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar(1, "sub-cliente"))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveLancarOperacaoInvalida_quandoTentaCancelarFilho_comoCliente() {
        Usuario usuario = usuarioCliente();
        Cliente cli = cliente(100);
        Agendamento filho = Agendamento.builder()
                .id(1)
                .agendamentoPaiId(5)
                .clienteId(100)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(filho));

        assertThatThrownBy(() -> useCase.executar(1, "sub-cliente"))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveCancelarComoAdmin_quandoAgendamentoAgendado() {
        Usuario admin = Usuario.builder().id(99).codKeycloak("sub-admin").role(RoleEnum.ADMIN).build();
        Agendamento agendamento = agendamentoPai(7, 100, StatusAgendamentoEnum.AGENDADO);
        Agenda ag = agenda(7);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();
        Cliente cli = cliente(100);

        when(usuarioPort.buscarPorCodKeycloak("sub-admin")).thenReturn(Optional.of(admin));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(cli));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-admin");

        assertThat(result).isNotNull();
        verify(agendamentoPort).salvar(any());
    }
}

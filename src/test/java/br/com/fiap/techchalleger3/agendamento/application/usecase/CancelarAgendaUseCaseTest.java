package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelarAgendaUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private CancelarAgendaUseCase useCase;

    private Agenda agenda(int vinculoId) {
        return Agenda.builder().id(1).profissionalVinculoId(vinculoId)
                .dataAgenda(LocalDate.now().plusDays(1)).build();
    }

    private Agendamento agendamento(StatusAgendamentoEnum status, Integer clienteId, Integer servicoId) {
        return Agendamento.builder().id(10).agendaId(1).agendamentoPaiId(null)
                .status(status).clienteId(clienteId).servicoId(servicoId)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5)).build();
    }

    // ── isAdmin=true, AGENDADO, clienteId+servicoId not null, vinculo found ─────

    @Test
    void isAdmin_agendado_comClienteEServico_enviaEmail() {
        Agenda ag = agenda(20);
        Agendamento a = agendamento(StatusAgendamentoEnum.AGENDADO, 100, 5);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();
        Cliente cliente = Cliente.builder().id(100).email("c@x.com").build();
        Servico servico = Servico.builder().id(5).nome("Corte").build();
        Profissional profissional = Profissional.builder().id(30).nome("Dr. João").build();

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(a));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(profissional));

        useCase.executar(1, "sub-admin", true);

        verify(emailSenderPort).enviar(any());
    }

    // ── isAdmin=true, AGENDADO, clienteId null → no email ────────────────────────

    @Test
    void isAdmin_agendado_semCliente_naoEnviaEmail() {
        Agenda ag = agenda(20);
        Agendamento a = agendamento(StatusAgendamentoEnum.AGENDADO, null, null);

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(a));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar(1, "sub-admin", true);

        verify(emailSenderPort, never()).enviar(any());
    }

    // ── isAdmin=true, DISPONIVEL → cancela sem email ──────────────────────────────

    @Test
    void isAdmin_disponivel_cancelaSemEmail() {
        Agenda ag = agenda(20);
        Agendamento a = agendamento(StatusAgendamentoEnum.DISPONIVEL, null, null);

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(a));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar(1, "sub-admin", true);

        verify(emailSenderPort, never()).enviar(any());
        verify(agendamentoPort).salvar(argThat(x -> StatusAgendamentoEnum.CANCELADO.equals(x.getStatus())));
    }

    // ── isAdmin=true, RESERVADO → cancela sem email ───────────────────────────────

    @Test
    void isAdmin_reservado_cancelaSemEmail() {
        Agenda ag = agenda(20);
        Agendamento a = agendamento(StatusAgendamentoEnum.RESERVADO, null, null);

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(a));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar(1, "sub-admin", true);

        verify(emailSenderPort, never()).enviar(any());
    }

    // ── isAdmin=false, profissional matches → cancela ─────────────────────────────

    @Test
    void profissional_posseValida_agendado_servicoNulo_vinculoNulo_enviaEmailSemDetalhes() {
        Agenda ag = agenda(20);
        Agendamento a = agendamento(StatusAgendamentoEnum.AGENDADO, 100, null);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();
        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(30).build();
        Cliente cliente = Cliente.builder().id(100).email("c@x.com").build();

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo), Optional.empty());
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(a));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(cliente));

        useCase.executar(1, "sub-prof", false);

        verify(emailSenderPort).enviar(any());
    }

    // ── isAdmin=false, profissional NÃO é dono → AcessoNegado ────────────────────

    @Test
    void profissional_posseInvalida_lancaAcessoNegado() {
        Agenda ag = agenda(20);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();
        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(99).build(); // different

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> useCase.executar(1, "sub-prof", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    // ── filho (agendamentoPaiId != null) é pulado via continue ───────────────────

    @Test
    void isAdmin_filhoNaLista_ehPulado() {
        Agenda ag = agenda(20);
        Agendamento filho = Agendamento.builder().id(11).agendaId(1).agendamentoPaiId(10)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agendamento pai = agendamento(StatusAgendamentoEnum.AGENDADO, null, null);

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(filho, pai));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar(1, "sub-admin", true);

        // filho is skipped, only pai (id=10) is saved as CANCELADO
        verify(agendamentoPort, times(1)).salvar(argThat(x -> StatusAgendamentoEnum.CANCELADO.equals(x.getStatus())));
    }

    // ── com filhos (buscarFilhosPorPaiId retorna lista) ───────────────────────────

    @Test
    void isAdmin_agendado_comFilhos_cancelaTodos() {
        Agenda ag = agenda(20);
        Agendamento pai = agendamento(StatusAgendamentoEnum.AGENDADO, 100, 5);
        Agendamento filho = Agendamento.builder().id(11).agendaId(1).agendamentoPaiId(10)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();
        Cliente cliente = Cliente.builder().id(100).email("c@x.com").build();

        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(ag));
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(pai));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of(filho));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        useCase.executar(1, "sub-admin", true);

        // pai + filho both saved as CANCELADO
        verify(agendamentoPort, times(2)).salvar(any());
    }
}

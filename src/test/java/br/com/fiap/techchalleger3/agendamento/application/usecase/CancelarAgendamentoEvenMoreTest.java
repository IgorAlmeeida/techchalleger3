package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAgendamentoEvenMoreTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private CancelarAgendamentoUseCase useCase;

    private Agenda agenda() {
        return Agenda.builder().id(7).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(20).build();
    }

    @Test
    void profissional_agendamentoPai_lancaOperacaoInvalida() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(30).build();
        Agendamento filho = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(5)
                .status(StatusAgendamentoEnum.AGENDADO).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(filho));

        assertThatThrownBy(() -> useCase.executar(1, "sub-prof"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Use o ID do agendamento pai");
    }

    @Test
    void profissional_semClienteId_naoEnviaEmail() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(30).build();
        Agendamento agendamento = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(null).servicoId(null)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Agendamento result = useCase.executar(1, "sub-prof");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
        verify(emailSenderPort, never()).enviar(any());
    }

    @Test
    void profissional_comFilhos_cancelaTodos() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(30).build();
        Agendamento pai = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(100).servicoId(5)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agendamento filho = Agendamento.builder().id(2).agendaId(7).agendamentoPaiId(1)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(pai));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of(filho));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(
                Cliente.builder().id(100).email("c@x.com").build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-prof");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void admin_agendamentoPai_lancaOperacaoInvalida() {
        Usuario admin = Usuario.builder().id(99).keycloakId("sub-admin").role(RoleEnum.ADMIN).build();
        Agendamento filho = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(5)
                .status(StatusAgendamentoEnum.AGENDADO).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-admin")).thenReturn(Optional.of(admin));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(filho));

        assertThatThrownBy(() -> useCase.executar(1, "sub-admin"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Use o ID do agendamento pai");
    }

    @Test
    void cliente_servicoNulo_vinculoNulo_emailSemDetalhes() {
        Usuario usuario = Usuario.builder().id(10).keycloakId("sub-cliente").role(RoleEnum.CLIENTE).build();
        Cliente cli = Cliente.builder().id(100).email("c@x.com").build();
        Agendamento agendamento = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(100).servicoId(null)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
        Agenda ag = Agenda.builder().id(7).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(20).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-cliente");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void cliente_comFilhos_cancelaTodos() {
        Usuario usuario = Usuario.builder().id(10).keycloakId("sub-cliente").role(RoleEnum.CLIENTE).build();
        Cliente cli = Cliente.builder().id(100).email("c@x.com").build();
        Agendamento pai = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(100).servicoId(5)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
        Agendamento filho = Agendamento.builder().id(2).agendaId(7).agendamentoPaiId(1)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-cliente")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(10)).thenReturn(Optional.of(cli));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(pai));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of(filho));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-cliente");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarAgendamentoMoreTest {

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
        return Usuario.builder().id(1).keycloakId("sub-1").build();
    }

    private Cliente cliente() {
        return Cliente.builder().id(10).email("c@x.com").build();
    }

    private Servico servico10min() {
        return Servico.builder().id(5).nome("Corte").duracaoMinutos(10).preco(BigDecimal.TEN).build();
    }

    @Test
    void lancaRegistroNaoEncontrado_usuarioAusente() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, null))
                .isInstanceOf(br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException.class);
    }

    @Test
    void lancaRegistroNaoEncontrado_clienteAusente() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, null))
                .isInstanceOf(br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException.class);
    }

    @Test
    void lancaAgendamentoJaExistente() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, null))
                .isInstanceOf(AgendamentoJaExistenteException.class);
    }

    @Test
    void lancaOperacaoInvalida_semHorariosDisponiveis() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void reservarSlotEspecifico_slotIndisponivel_lancaOperacaoInvalida() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);

        Agendamento slotAgendado = Agendamento.builder().id(99).agendaId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.of(slotAgendado));

        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, 99))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("não está disponível");
    }

    @Test
    void reservarSlotEspecifico_semJanelaConsecutiva_lancaOperacaoInvalida() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico10min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);

        Agendamento slot = Agendamento.builder().id(99).agendaId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.of(slot));
        when(agendamentoPort.listarPorAgendaId(10)).thenReturn(List.of(slot));

        // Serviço de 10min requer 2 slots de 5min mas só há 1
        assertThatThrownBy(() -> useCase.executar("sub", 1, 5, null, 99))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("slots consecutivos");
    }

    @Test
    void reservarGrupo_sucesso_comUmSlot() {
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(
                ProfissionalVinculo.builder().id(1).profissionalId(30).build()));
        Servico servico5min = Servico.builder().id(5).nome("Corte").duracaoMinutos(5).preco(BigDecimal.TEN).build();
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);

        Agendamento slot = Agendamento.builder().id(1).agendaId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(10), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(10).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(10)).thenReturn(Optional.of(agenda));
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda())).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(1);
            return a;
        });
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(
                Profissional.builder().id(30).nome("Dr.").build()));

        Agendamento result = useCase.executar("sub", 1, 5, null, null);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
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
class AgendarEmNomeDeClienteEvenMoreTest {

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

    private Cliente cliente() { return Cliente.builder().id(10).email("c@x.com").build(); }
    private ProfissionalVinculo vinculo() {
        return ProfissionalVinculo.builder().id(1).profissionalId(30).estabelecimentoId(5).build();
    }
    private Servico servico5min() {
        return Servico.builder().id(5).nome("Corte").duracaoMinutos(5).preco(BigDecimal.TEN).build();
    }
    private Servico servico10min() {
        return Servico.builder().id(5).nome("Corte").duracaoMinutos(10).preco(BigDecimal.TEN).build();
    }

    private void setupBase(Servico servico) {
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.of(cliente()));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);
    }

    // ── reservarSlotEspecifico: slot não DISPONIVEL ───────────────────────────

    @Test
    void reservarSlotEspecifico_statusNaoDisponivel_lancaOperacaoInvalida() {
        setupBase(servico5min());
        Agendamento slot = Agendamento.builder().id(99).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, 99))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("não está disponível");
    }

    // ── reservarSlotEspecifico: janela vazia → lança OperacaoInvalida ─────────

    @Test
    void reservarSlotEspecifico_janelaVazia_lancaOperacaoInvalida() {
        setupBase(servico10min());
        // Single 5-min slot: service is 10min (2 slots), but only 1 available → janela empty
        Agendamento slot = Agendamento.builder().id(99).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.of(slot));
        when(agendamentoPort.listarPorAgendaId(20)).thenReturn(List.of(slot)); // only 1 slot available

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, 99))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("slots consecutivos");
    }

    // ── reservarGrupo: múltiplos slots (loop de filhos i=1) ───────────────────

    @Test
    void reservarGrupo_multiplosSlotsConsecutivos_sucesso() {
        setupBase(servico10min());
        Agendamento slot1 = Agendamento.builder().id(1).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        Agendamento slot2 = Agendamento.builder().id(2).agendaId(20)
                .horaInicio(LocalTime.of(9, 5)).horaFim(LocalTime.of(9, 10))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot1, slot2));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda())).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            if (a.getId() == null) a.setId(100);
            return a;
        });
        // profissionalVinculoPort is already stubbed for initial check
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar("111", null, null, null, null, null, null, 1, 5, null);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }

    // ── reservarGrupo: vinculo null → profissionalNome = "Profissional" ────────

    @Test
    void reservarGrupo_vinculoNull_usaNomePadrao() {
        setupBase(servico5min());
        Agendamento slot = Agendamento.builder().id(1).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(99).build(); // vinculoId=99, not stubbed → returns empty
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda())).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(1);
            return a;
        });
        when(profissionalVinculoPort.buscarPorId(99)).thenReturn(Optional.empty()); // vinculo == null

        Agendamento result = useCase.executar("111", null, null, null, null, null, null, 1, 5, null);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }

    // ── filtrarAgendas: serviço não presente na agenda → filtered out ──────────

    @Test
    void filtrarAgendas_servicoNaoPresente_lancaOperacaoInvalida() {
        setupBase(servico5min());
        Agendamento slot = Agendamento.builder().id(1).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(false); // service not in agenda

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Sem horários disponíveis");
    }

    // ── reservarGrupo: conflito não ocorre (loop runs, condition false) ─────────

    @Test
    void reservarGrupo_conflitoNaoSobrepoe_sucesso() {
        setupBase(servico5min());
        Agendamento slot = Agendamento.builder().id(1).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));

        // Existing agendamento that does NOT overlap: 10:00-11:00, new is 09:00-09:05
        Agendamento naoConflito = Agendamento.builder().id(99)
                .horaInicio(LocalTime.of(10, 0)).horaFim(LocalTime.of(11, 0)).build();
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda()))
                .thenReturn(List.of(naoConflito));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(1);
            return a;
        });
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar("111", null, null, null, null, null, null, 1, 5, null);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }
}

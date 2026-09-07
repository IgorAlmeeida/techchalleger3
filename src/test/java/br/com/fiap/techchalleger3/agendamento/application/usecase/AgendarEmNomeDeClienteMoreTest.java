package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ConflitoDeHorarioClienteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.EmailJaCadastradoException;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendarEmNomeDeClienteMoreTest {

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

    private Servico servico5min() {
        return Servico.builder().id(5).nome("Corte").duracaoMinutos(5).preco(BigDecimal.TEN).build();
    }

    private ProfissionalVinculo vinculo() {
        return ProfissionalVinculo.builder().id(1).profissionalId(30).estabelecimentoId(10).build();
    }

    // ── resolverOuCriarCliente ─────────────────────────────────────────────

    @Test
    void cpfNaoEncontrado_semDadosObrigatorios_lancaOperacaoInvalida() {
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("nome, dataNascimento e email");
    }

    @Test
    void cpfNaoEncontrado_emailJaCadastrado_lancaEmailJaCadastrado() {
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.empty());
        when(clientePort.buscarPorEmail("e@x.com")).thenReturn(
                Optional.of(Cliente.builder().id(99).email("e@x.com").build()));

        assertThatThrownBy(() -> useCase.executar(
                "111", "João", LocalDate.of(1990, 1, 1), "11999", "M", "Rua A", "e@x.com", 1, 5, null))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void cpfNaoEncontrado_criaClienteNovo_sucesso() {
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.empty());
        when(clientePort.buscarPorEmail("new@x.com")).thenReturn(Optional.empty());
        when(keycloakAdminPort.criarUsuario(any(), any(), any(), any(), anyBoolean())).thenReturn("kc-uid");
        when(usuarioPort.salvar(any())).thenReturn(Usuario.builder().id(50).keycloakId("kc-uid").build());

        Cliente clienteNovo = Cliente.builder().id(77).email("new@x.com").build();
        when(clientePort.salvar(any())).thenReturn(clienteNovo);

        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(77, 1, 5)).thenReturn(false);

        Agendamento slot = Agendamento.builder().id(10).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));
        when(agendamentoPort.buscarAgendadosPorClienteNaData(77, agenda.getDataAgenda())).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(100);
            return a;
        });
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(
                "111", "João", LocalDate.of(1990, 1, 1), "11999", "M", "Rua A", "new@x.com", 1, 5, null);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
        verify(emailSenderPort, atLeast(2)).enviar(any()); // cadastro + confirmação de agendamento
    }

    // ── agendamentoJaExistente ─────────────────────────────────────────────

    @Test
    void clienteExistente_agendamentoJaExistente_lancaException() {
        Cliente cliente = Cliente.builder().id(10).email("c@x.com").build();
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(AgendamentoJaExistenteException.class);
    }

    // ── reservarSlotEspecifico ─────────────────────────────────────────────

    @Test
    void reservarSlotEspecifico_sucesso() {
        Cliente cliente = Cliente.builder().id(10).email("c@x.com").build();
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);

        Agendamento slot = Agendamento.builder().id(99).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.of(slot));
        when(agendamentoPort.listarPorAgendaId(20)).thenReturn(List.of(slot));

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda())).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(99);
            return a;
        });
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar("111", null, null, null, null, null, null, 1, 5, 99);

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }

    // ── reservarGrupo: conflito de horário do cliente ──────────────────────

    @Test
    void reservarGrupo_conflitoHorario_lancaException() {
        Cliente cliente = Cliente.builder().id(10).email("c@x.com").build();
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);

        Agendamento slot = Agendamento.builder().id(1).agendaId(20)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(slot));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(20), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(20).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(1).build();
        when(agendaPort.buscarPorId(20)).thenReturn(Optional.of(agenda));

        // conflito: cliente já tem agendamento das 08:55 às 09:03 (sobrepõe 09:00–09:05)
        Agendamento conflito = Agendamento.builder().id(99)
                .horaInicio(LocalTime.of(8, 55)).horaFim(LocalTime.of(9, 3)).build();
        when(agendamentoPort.buscarAgendadosPorClienteNaData(10, agenda.getDataAgenda()))
                .thenReturn(List.of(conflito));

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(ConflitoDeHorarioClienteException.class);
    }

    // ── sem horários disponíveis ───────────────────────────────────────────

    @Test
    void semHorariosDisponiveis_lancaOperacaoInvalida() {
        Cliente cliente = Cliente.builder().id(10).email("c@x.com").build();
        when(clientePort.buscarPorCpf("111")).thenReturn(Optional.of(cliente));
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico5min()));
        when(agendamentoPort.existeAgendadoPorClienteVinculoServico(10, 1, 5)).thenReturn(false);
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar("111", null, null, null, null, null, null, 1, 5, null))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Sem horários disponíveis");
    }
}

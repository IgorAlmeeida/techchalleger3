package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5778")
@ExtendWith(MockitoExtension.class)
class AgendaUseCaseTest {

    // Shared mocks
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private EmailSenderPort emailSenderPort;

    // GerarAgendaUseCase
    @InjectMocks private GerarAgendaUseCase gerarAgenda;

    // CancelarAgendaUseCase
    @InjectMocks private CancelarAgendaUseCase cancelarAgenda;

    // ListarAgendasUseCase
    @InjectMocks private ListarAgendasUseCase listarAgendas;

    @Test
    void gerar_lancaQuandoEscalaNaoExiste() {
        when(escalaPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerarAgenda.executar(99, LocalDate.now(), LocalDate.now().plusDays(7), "x", true))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void gerar_lancaQuandoSemItensAtivos() {
        Escala escala = Escala.builder().id(1).diaSemana(DiaSemanaEnum.SEGUNDA).profissionalVinculoId(10).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> gerarAgenda.executar(1, LocalDate.now(), LocalDate.now().plusDays(7), "x", true))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void gerar_lancaQuandoNenhumaDataNoDia() {
        // SEGUNDA in range that has no MONDAY
        LocalDate start = LocalDate.of(2026, 9, 1); // Tuesday
        LocalDate end = LocalDate.of(2026, 9, 6);   // Sunday — no Monday in range
        Escala escala = Escala.builder().id(1).diaSemana(DiaSemanaEnum.SEGUNDA).profissionalVinculoId(10)
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(9,5)).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(EscalaItem.builder().id(1).servicoId(5).build()));

        assertThatThrownBy(() -> gerarAgenda.executar(1, start, end, "x", true))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void gerar_criaAgendaParaDataCorreta() {
        // SEGUNDA = Monday; 2026-09-07 is a Monday
        LocalDate monday = LocalDate.of(2026, 9, 7);
        Escala escala = Escala.builder().id(1).diaSemana(DiaSemanaEnum.SEGUNDA).profissionalVinculoId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10)).estabelecimentoId(20).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(EscalaItem.builder().id(1).servicoId(5).build()));
        when(agendaPort.existeAgendaPorVinculoEData(10, monday)).thenReturn(false);
        Agenda agenda = Agenda.builder().id(100).profissionalVinculoId(10).horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(9,10)).build();
        when(agendaPort.salvar(any())).thenReturn(agenda);
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Agenda> result = gerarAgenda.executar(1, monday, monday, "x", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void cancelar_admin_cancelaTodosDisponiveisESemPai() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(10,0)).build();
        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(agenda));

        Agendamento disp = Agendamento.builder().id(10).agendaId(1).status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(disp));

        cancelarAgenda.executar(1, "admin", true);

        verify(agendamentoPort).salvar(disp);
    }

    @Test
    void cancelar_profissional_lancaAcessoNegado_quandoVinculoDeOutro() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).build();
        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(agenda));
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(5).build()));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(Profissional.builder().id(99).build()));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(10).profissionalId(50).build()));

        assertThatThrownBy(() -> cancelarAgenda.executar(1, "kc", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void listar_lancaQuandoSemFiltro() {
        assertThatThrownBy(() -> listarAgendas.executar(null, null, null, null, "x", true, PageRequest.of(0, 10)))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void cancelar_admin_agendado_enviaNota_paraCliente() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(10,0)).build();
        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(agenda));

        Agendamento agendado = Agendamento.builder().id(10).agendaId(1).clienteId(50).servicoId(99)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(agendado));
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of());
        when(clientePort.buscarPorId(50)).thenReturn(Optional.of(
                br.com.fiap.techchalleger3.agendamento.domain.model.Cliente.builder().id(50).email("c@x.com").build()));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(10).profissionalId(20).build()));

        cancelarAgenda.executar(1, "admin", true);

        verify(emailSenderPort).enviar(any());
    }

    @Test
    void listar_admin_retornaPagina() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        Page<Agenda> page = new PageImpl<>(List.of(agenda));
        when(agendaPort.listarPorFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(Profissional.builder().id(20).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));
        when(agendaItemPort.listarPorAgendaId(1)).thenReturn(List.of());

        var result = listarAgendas.executar(null, 30, null, null, "x", true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listar_admin_retornaPagina_comServicos() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        Page<Agenda> page = new PageImpl<>(List.of(agenda));
        when(agendaPort.listarPorFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(Profissional.builder().id(20).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));

        AgendaItem item = AgendaItem.builder().id(1).agendaId(1).servicoId(5).build();
        when(agendaItemPort.listarPorAgendaId(1)).thenReturn(List.of(item));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(Servico.builder().id(5).nome("Corte").duracaoMinutos(30).build()));

        var result = listarAgendas.executar(null, 30, null, null, "x", true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listar_profissional_retornaPropriosVinculos() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).build()));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Profissional.builder().id(10).build()));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(5).profissionalId(10).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(5)).thenReturn(Optional.of(vinculo));

        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(5).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        Page<Agenda> page = new PageImpl<>(List.of(agenda));
        when(agendaPort.listarPorFiltros(any(), any(), any(), any(), any())).thenReturn(page);
        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(Profissional.builder().id(10).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));
        when(agendaItemPort.listarPorAgendaId(1)).thenReturn(List.of());

        var result = listarAgendas.executar(5, null, null, null, "kc", false, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listar_profissional_lancaAcessoNegado_quandoVinculoDeOutro() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).build()));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Profissional.builder().id(10).build()));
        ProfissionalVinculo outroVinculo = ProfissionalVinculo.builder().id(5).profissionalId(99).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(5)).thenReturn(Optional.of(outroVinculo));

        assertThatThrownBy(() -> listarAgendas.executar(5, null, null, null, "kc", false, PageRequest.of(0, 10)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void cancelar_admin_comFilhosEReservado() {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10).dataAgenda(LocalDate.now())
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(10,0)).build();
        when(agendaPort.buscarPorId(1)).thenReturn(Optional.of(agenda));

        // filho com paiId não-nulo → continue (skipped)
        Agendamento filho = Agendamento.builder().id(20).agendaId(1).agendamentoPaiId(10)
                .status(StatusAgendamentoEnum.DISPONIVEL).build();
        // AGENDADO pai sem clienteId
        Agendamento agendadoPai = Agendamento.builder().id(10).agendaId(1)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        // RESERVADO
        Agendamento reservado = Agendamento.builder().id(11).agendaId(1)
                .status(StatusAgendamentoEnum.RESERVADO).build();

        when(agendamentoPort.listarPorAgendaId(1)).thenReturn(List.of(filho, agendadoPai, reservado));

        Agendamento filhoExt = Agendamento.builder().id(30).status(StatusAgendamentoEnum.AGENDADO).build();
        when(agendamentoPort.buscarFilhosPorPaiId(10)).thenReturn(List.of(filhoExt));

        cancelarAgenda.executar(1, "admin", true);

        verify(agendamentoPort, atLeastOnce()).salvar(any());
    }
}

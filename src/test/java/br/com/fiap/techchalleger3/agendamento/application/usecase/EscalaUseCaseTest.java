package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ItemNaoPermitidoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EscalaDetalhadaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5778")
@ExtendWith(MockitoExtension.class)
class EscalaUseCaseTest {

    // ListarEscalasUseCase
    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @InjectMocks private ListarEscalasUseCase listarEscalas;

    // AtualizarEscalaUseCase
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @InjectMocks private AtualizarEscalaUseCase atualizarEscala;

    @Test
    void listar_lancaQuandoSemFiltro() {
        assertThatThrownBy(() -> listarEscalas.executar(null, null, "x", true))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void listar_admin_retornaResultados() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10).diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(17, 0)).build();
        when(escalaPort.listarPorFiltros(5, null)).thenReturn(List.of(escala));

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(Profissional.builder().id(20).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of());

        List<EscalaDetalhadaResponse> result = listarEscalas.executar(5, null, "any", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void listar_profissional_lancaAcessoNegado_quandoVinculoDeOutro() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(5).build()));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(Profissional.builder().id(99).build()));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(50).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));

        assertThatThrownBy(() -> listarEscalas.executar(null, 10, "kc", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void atualizar_lancaQuandoEscalaNaoExiste() {
        when(escalaPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atualizarEscala.executar(99, DiaSemanaEnum.SEGUNDA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(), "x", true))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void atualizar_lancaQuandoServicoNaoPermitido() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of());

        assertThatThrownBy(() -> atualizarEscala.executar(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(99), "x", true))
                .isInstanceOf(ItemNaoPermitidoException.class);
    }

    @Test
    void atualizar_salvaQuandoValido_semServicos() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10).diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of());
        when(profissionalVinculoPort.listarPorProfissionalId(20)).thenReturn(List.of());
        when(escalaPort.salvar(any())).thenReturn(escala);

        Escala result = atualizarEscala.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(), "x", true);

        assertThat(result).isNotNull();
        verify(escalaItemPort).deletarPorEscalaId(1);
    }

    @Test
    void atualizar_profissional_lancaAcessoNegado_quandoVinculoDeOutro() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA).horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(5).build()));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(Profissional.builder().id(99).build()));

        assertThatThrownBy(() -> atualizarEscala.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(), "kc", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void atualizar_profissional_salvaQuandoProprioVinculo() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA).horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(5).build()));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(Profissional.builder().id(20).build()));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of());
        when(profissionalVinculoPort.listarPorProfissionalId(20)).thenReturn(List.of());
        when(escalaPort.salvar(any())).thenReturn(escala);

        Escala result = atualizarEscala.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(), "kc", false);

        assertThat(result).isNotNull();
    }

    @Test
    void atualizar_salvaQuandoValido_comServicos() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA).horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10))
                .thenReturn(List.of(ProfissionalVinculoServico.builder().servicoId(5).build()));
        when(profissionalVinculoPort.listarPorProfissionalId(20)).thenReturn(List.of());
        when(escalaPort.salvar(any())).thenReturn(escala);
        when(escalaItemPort.salvar(any())).thenReturn(EscalaItem.builder().id(1).build());

        Escala result = atualizarEscala.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(8,0), LocalTime.of(12,0), List.of(5), "x", true);

        assertThat(result).isNotNull();
        verify(escalaItemPort).salvar(any());
    }

    @Test
    void listar_admin_retornaResultados_comServicos() {
        Escala escala = Escala.builder().id(1).profissionalVinculoId(10).diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(17,0)).build();
        when(escalaPort.listarPorFiltros(5, null)).thenReturn(List.of(escala));

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(Profissional.builder().id(20).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));

        EscalaItem item = EscalaItem.builder().id(1).servicoId(5).build();
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(item));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(Servico.builder().id(5).nome("Corte").duracaoMinutos(30).build()));

        List<EscalaDetalhadaResponse> result = listarEscalas.executar(5, null, "any", true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).servicos()).hasSize(1);
    }
}

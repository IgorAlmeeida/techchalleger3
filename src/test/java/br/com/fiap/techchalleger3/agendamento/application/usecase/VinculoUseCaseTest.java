package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendaEmAbertoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VinculoUseCaseTest {

    // CriarVinculoUseCase
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @InjectMocks private CriarVinculoUseCase criarVinculo;

    // ListarVinculosUseCase
    @InjectMocks private ListarVinculosUseCase listarVinculos;

    // DesvincularProfissionalUseCase
    @Mock private AgendaRepositoryPort agendaPort;
    @InjectMocks private DesvincularProfissionalUseCase desvincular;

    // AssociarServicoAoVinculoUseCase
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @Mock private CachePort cachePort;
    @InjectMocks private AssociarServicoAoVinculoUseCase associarServico;

    // DesvincularItemUseCase
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @InjectMocks private DesvincularItemUseCase desvincularItem;

    @Test
    void criarVinculo_quandoValido() {
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(Profissional.builder().id(1).build()));
        when(estabelecimentoPort.buscarPorId(2)).thenReturn(Optional.of(Estabelecimento.builder().id(2).build()));
        when(profissionalVinculoPort.existeVinculoAtivo(1, 2)).thenReturn(false);
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).build();
        when(profissionalVinculoPort.salvar(any())).thenReturn(vinculo);

        criarVinculo.executar(1, 2, LocalDate.now());

        verify(profissionalVinculoPort).salvar(any());
    }

    @Test
    void criarVinculo_lancaQuandoProfissionalNaoExiste() {
        when(profissionalPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> criarVinculo.executar(99, 2, LocalDate.now()))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void criarVinculo_lancaQuandoJaExiste() {
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(Profissional.builder().id(1).build()));
        when(estabelecimentoPort.buscarPorId(2)).thenReturn(Optional.of(Estabelecimento.builder().id(2).build()));
        when(profissionalVinculoPort.existeVinculoAtivo(1, 2)).thenReturn(true);

        assertThatThrownBy(() -> criarVinculo.executar(1, 2, LocalDate.now()))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void listarVinculos_delegaParaPort() {
        when(profissionalVinculoPort.listarPorFiltros(any(), any(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        listarVinculos.executar(1, 2, PageRequest.of(0, 10));

        verify(profissionalVinculoPort).listarPorFiltros(1, 2, PageRequest.of(0, 10));
    }

    @Test
    void desvincular_lancaQuandoVinculoNaoExiste() {
        when(profissionalVinculoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desvincular.executar(99))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void desvincular_lancaQuandoAgendaEmAberto() {
        when(profissionalVinculoPort.buscarPorId(5)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(5).build()));
        when(agendaPort.existeAgendaFuturaPorVinculo(5)).thenReturn(true);

        assertThatThrownBy(() -> desvincular.executar(5))
                .isInstanceOf(AgendaEmAbertoException.class);
    }

    @Test
    void desvincular_salvaComDataFim_quandoValido() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(5).build();
        when(profissionalVinculoPort.buscarPorId(5)).thenReturn(Optional.of(vinculo));
        when(agendaPort.existeAgendaFuturaPorVinculo(5)).thenReturn(false);

        desvincular.executar(5);

        verify(profissionalVinculoPort).salvar(vinculo);
    }

    @Test
    void associarServico_lancaQuandoVinculoNaoExiste() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associarServico.executar(1, 2))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void associarServico_quandoValido() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(2)).thenReturn(Optional.of(Servico.builder().id(2).build()));
        when(vinculoServicoPort.existePorVinculoEServico(1, 2)).thenReturn(false);
        when(vinculoServicoPort.salvar(any())).thenReturn(ProfissionalVinculoServico.builder().id(10).build());

        associarServico.executar(1, 2);

        verify(cachePort).invalidar(any());
    }

    @Test
    void desvincularItem_lancaQuandoVinculoNaoExiste() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desvincularItem.executar(1, 2))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void desvincularItem_quandoValido_semAgendaFutura() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(vinculoServicoPort.existePorVinculoEServico(1, 2)).thenReturn(true);
        when(agendaPort.listarFuturasPorVinculo(1)).thenReturn(List.of());

        desvincularItem.executar(1, 2);

        verify(vinculoServicoPort).deletarPorVinculoEServico(1, 2);
        verify(cachePort).invalidar(any());
    }

    @Test
    void associarServico_lancaQuandoJaAssociado() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(servicoPort.buscarPorId(2)).thenReturn(Optional.of(Servico.builder().id(2).build()));
        when(vinculoServicoPort.existePorVinculoEServico(1, 2)).thenReturn(true);

        assertThatThrownBy(() -> associarServico.executar(1, 2))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void desvincularItem_lancaQuandoServicoNaoAssociado() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(vinculoServicoPort.existePorVinculoEServico(1, 2)).thenReturn(false);

        assertThatThrownBy(() -> desvincularItem.executar(1, 2))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void desvincularItem_lancaQuandoAgendaFuturaUsaServico() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(ProfissionalVinculo.builder().id(1).build()));
        when(vinculoServicoPort.existePorVinculoEServico(1, 2)).thenReturn(true);
        Agenda agendaFutura = Agenda.builder().id(50).build();
        when(agendaPort.listarFuturasPorVinculo(1)).thenReturn(List.of(agendaFutura));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(50), 2)).thenReturn(true);

        assertThatThrownBy(() -> desvincularItem.executar(1, 2))
                .isInstanceOf(AgendaEmAbertoException.class);
    }
}

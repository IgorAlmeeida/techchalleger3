package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliarAtendimentoUseCaseTest {

    @Mock private AvaliacaoRepositoryPort avaliacaoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;

    @InjectMocks private AvaliarAtendimentoUseCase useCase;

    @Test
    void deveLancarExcecao_quandoNotaMenorQueUm() {
        assertThatThrownBy(() -> useCase.avaliar(1, 1, 0, "comentário"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("entre 1 e 5");
        verify(agendamentoPort, never()).buscarPorId(any());
    }

    @Test
    void deveLancarExcecao_quandoNotaMaiorQueCinco() {
        assertThatThrownBy(() -> useCase.avaliar(1, 1, 6, "comentário"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("entre 1 e 5");
    }

    @Test
    void deveLancarExcecao_quandoAgendamentoNaoEncontrado() {
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.avaliar(1, 1, 5, "ok"))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoClienteNaoAutorizado() {
        Agendamento agendamento = Agendamento.builder()
                .id(1).clienteId(1).status(StatusAgendamentoEnum.REALIZADO).agendaId(10).build();
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.avaliar(1, 2, 5, "ok"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("não autorizado");
    }

    @Test
    void deveLancarExcecao_quandoStatusNaoRealizado() {
        Agendamento agendamento = Agendamento.builder()
                .id(1).clienteId(1).status(StatusAgendamentoEnum.AGENDADO).agendaId(10).build();
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.avaliar(1, 1, 5, "ok"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("realizados");
    }

    @Test
    void deveLancarExcecao_quandoJaFoiAvaliado() {
        Agendamento agendamento = Agendamento.builder()
                .id(1).clienteId(1).status(StatusAgendamentoEnum.REALIZADO).agendaId(10).build();
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(avaliacaoPort.existePorAgendamentoId(1)).thenReturn(true);

        assertThatThrownBy(() -> useCase.avaliar(1, 1, 5, "ok"))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("já foi avaliado");
    }

    @Test
    void deveRegistrarAvaliacao_quandoDadosValidos() {
        Agendamento agendamento = Agendamento.builder()
                .id(1).clienteId(1).status(StatusAgendamentoEnum.REALIZADO).agendaId(10).build();
        Agenda agenda = Agenda.builder().id(10).estabelecimentoId(5).profissionalVinculoId(3).build();
        Avaliacao salva = Avaliacao.builder().id(99).agendamentoId(1).clienteId(1).nota(5).build();

        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(avaliacaoPort.existePorAgendamentoId(1)).thenReturn(false);
        when(agendaPort.buscarPorId(10)).thenReturn(Optional.of(agenda));
        when(avaliacaoPort.salvar(any(Avaliacao.class))).thenReturn(salva);

        Avaliacao resultado = useCase.avaliar(1, 1, 5, "Excelente");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(99);
        verify(avaliacaoPort).salvar(any(Avaliacao.class));
    }
}

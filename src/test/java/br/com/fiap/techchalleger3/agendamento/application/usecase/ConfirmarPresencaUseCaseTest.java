package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmarPresencaUseCaseTest {

    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;

    @InjectMocks private ConfirmarPresencaUseCase useCase;

    @Test
    void deveConfirmarPresenca_quandoAgendamentoAgendadoEPai() {
        Agendamento agendamento = Agendamento.builder()
                .id(1)
                .agendaId(10)
                .agendamentoPaiId(null)
                .status(StatusAgendamentoEnum.AGENDADO)
                .presencaConfirmada(false)
                .build();

        Agendamento salvo = Agendamento.builder()
                .id(1)
                .agendaId(10)
                .presencaConfirmada(true)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();

        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendamentoPort.salvar(any())).thenReturn(salvo);
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendaPort.buscarPorId(10)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1);

        assertThat(result.getPresencaConfirmada()).isTrue();
        verify(agendamentoPort).salvar(any());
    }

    @Test
    void deveLancarExcecao_quandoAgendamentoNaoEncontrado() {
        when(agendamentoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(99))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoAgendamentoFilho() {
        Agendamento filho = Agendamento.builder()
                .id(2)
                .agendamentoPaiId(5)
                .status(StatusAgendamentoEnum.AGENDADO)
                .build();

        when(agendamentoPort.buscarPorId(2)).thenReturn(Optional.of(filho));

        assertThatThrownBy(() -> useCase.executar(2))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveLancarExcecao_quandoStatusNaoAgendado() {
        Agendamento cancelado = Agendamento.builder()
                .id(3)
                .agendamentoPaiId(null)
                .status(StatusAgendamentoEnum.CANCELADO)
                .build();

        when(agendamentoPort.buscarPorId(3)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> useCase.executar(3))
                .isInstanceOf(OperacaoInvalidaException.class);
    }
}

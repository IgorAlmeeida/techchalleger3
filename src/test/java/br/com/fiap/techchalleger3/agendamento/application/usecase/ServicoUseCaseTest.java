package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoUseCaseTest {

    @Mock private ServicoRepositoryPort servicoPort;
    @InjectMocks private ServicoUseCase useCase;

    private Servico servico(int id) {
        return Servico.builder().id(id).nome("Corte " + id).duracaoMinutos(30).preco(BigDecimal.TEN).ativo(true).build();
    }

    @Test
    void deveCriar_quandoDuracaoMultiploDe5() {
        when(servicoPort.salvar(any())).thenReturn(servico(1));

        Servico result = useCase.criar("Corte", 30, BigDecimal.TEN);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deveLancar_quandoDuracaoNaoMultiploDe5() {
        assertThatThrownBy(() -> useCase.criar("X", 7, BigDecimal.TEN))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveLancar_quandoDuracaoNula() {
        assertThatThrownBy(() -> useCase.criar("X", null, BigDecimal.TEN))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveListar_doBanco() {
        when(servicoPort.listarAtivos()).thenReturn(List.of(servico(1), servico(2)));

        Page<Servico> page = useCase.listar(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void deveAtualizar_quandoValido() {
        Servico existente = servico(1);
        when(servicoPort.buscarPorId(1)).thenReturn(Optional.of(existente));
        when(servicoPort.salvar(any())).thenReturn(existente);

        Servico result = useCase.atualizar(1, "Novo Corte", 45, BigDecimal.valueOf(50));

        assertThat(result).isNotNull();
    }

    @Test
    void deveDeletar() {
        Servico existente = servico(1);
        when(servicoPort.buscarPorId(1)).thenReturn(Optional.of(existente));

        useCase.deletar(1);

        verify(servicoPort).deletar(1);
    }
}

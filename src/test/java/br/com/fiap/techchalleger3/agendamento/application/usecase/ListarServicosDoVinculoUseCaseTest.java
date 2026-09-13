package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoItemDetalhadaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarServicosDoVinculoUseCaseTest {

    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @InjectMocks private ListarServicosDoVinculoUseCase useCase;

    @Test
    void deveBuscarEMontarResposta() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20).build();
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(Profissional.builder().id(10).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(20)).thenReturn(Optional.of(Estabelecimento.builder().id(20).nome("Studio").build()));

        ProfissionalVinculoServico vs = ProfissionalVinculoServico.builder().id(5).profissionalVinculoId(1).servicoId(30).build();
        when(vinculoServicoPort.listarPorProfissionalVinculoId(1)).thenReturn(List.of(vs));
        when(servicoPort.buscarPorId(30)).thenReturn(Optional.of(Servico.builder().id(30).nome("Corte").duracaoMinutos(30).preco(BigDecimal.TEN).build()));

        List<VinculoItemDetalhadaResponse> result = useCase.executar(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void deveLancarQuandoVinculoNaoExiste() {
        when(profissionalVinculoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(99))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveRetornarListaVaziaQuandoSemServicos() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20).build();
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(Profissional.builder().id(10).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(20)).thenReturn(Optional.of(Estabelecimento.builder().id(20).nome("Studio").build()));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(1)).thenReturn(List.of());

        List<VinculoItemDetalhadaResponse> result = useCase.executar(1);

        assertThat(result).isEmpty();
    }
}

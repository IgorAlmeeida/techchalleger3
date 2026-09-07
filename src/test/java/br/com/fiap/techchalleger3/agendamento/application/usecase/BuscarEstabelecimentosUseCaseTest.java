package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarEstabelecimentosUseCaseTest {

    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;

    @InjectMocks private BuscarEstabelecimentosUseCase useCase;

    @Test
    void deveDelegarAoPort_quandoBuscaComFiltros() {
        Estabelecimento estab = Estabelecimento.builder().id(1).nome("Studio Bella").build();
        Page<Estabelecimento> pagina = new PageImpl<>(List.of(estab));
        Pageable pageable = PageRequest.of(0, 20);

        when(estabelecimentoPort.buscarComFiltros(
                eq("Studio"), eq("SP"), eq(1), eq(BigDecimal.valueOf(30)),
                eq(BigDecimal.valueOf(100)), eq(4.0), isNull(), eq(pageable)))
                .thenReturn(pagina);

        Page<Estabelecimento> resultado = useCase.buscar(
                "Studio", "SP", 1, BigDecimal.valueOf(30), BigDecimal.valueOf(100), 4.0, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Studio Bella");
        verify(estabelecimentoPort).buscarComFiltros(
                eq("Studio"), eq("SP"), eq(1), eq(BigDecimal.valueOf(30)),
                eq(BigDecimal.valueOf(100)), eq(4.0), isNull(), eq(pageable));
    }

    @Test
    void devePassarFiltrosNulos_quandoSemFiltros() {
        Pageable pageable = PageRequest.of(0, 10);
        when(estabelecimentoPort.buscarComFiltros(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(Page.empty());

        Page<Estabelecimento> resultado = useCase.buscar(null, null, null, null, null, null, null, pageable);

        assertThat(resultado).isNotNull();
        verify(estabelecimentoPort).buscarComFiltros(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }
}

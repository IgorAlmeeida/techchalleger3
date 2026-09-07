package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BuscarEstabelecimentosUseCase {

    private final EstabelecimentoRepositoryPort estabelecimentoPort;

    public Page<Estabelecimento> buscar(
            String nome,
            String localizacao,
            Integer servicoId,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Double notaMinima,
            LocalDate data,
            Pageable pageable) {
        return estabelecimentoPort.buscarComFiltros(
                nome, localizacao, servicoId, precoMin, precoMax, notaMinima, data, pageable);
    }
}

package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarVinculosUseCase {

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;

    public Page<ProfissionalVinculo> executar(Integer profissionalId, Integer estabelecimentoId, Pageable pageable) {
        return profissionalVinculoPort.listarPorFiltros(profissionalId, estabelecimentoId, pageable);
    }
}

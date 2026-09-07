package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProfissionalVinculoRepositoryPort {
    Optional<ProfissionalVinculo> buscarPorId(Integer id);
    List<ProfissionalVinculo> listarPorProfissionalId(Integer profissionalId);
    List<Integer> listarEstabelecimentoIdsAtivosPorProfissional(Integer profissionalId);
    List<Integer> listarIdsPorEstabelecimento(Integer estabelecimentoId);
    Page<ProfissionalVinculo> listarPorFiltros(Integer profissionalId, Integer estabelecimentoId, Pageable pageable);
    boolean existeVinculoAtivo(Integer profissionalId, Integer estabelecimentoId);
    boolean existeVinculoAtivoPorEstabelecimento(Integer estabelecimentoId);
    ProfissionalVinculo salvar(ProfissionalVinculo vinculo);
}

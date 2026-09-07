package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ProfissionalVinculoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfissionalVinculoRepositoryPortImpl implements ProfissionalVinculoRepositoryPort {

    private final ProfissionalVinculoRepository repository;
    private final ProfissionalVinculoMapper mapper;

    @Override
    public Optional<ProfissionalVinculo> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<ProfissionalVinculo> listarPorProfissionalId(Integer profissionalId) {
        return repository.findByCodProfissional(profissionalId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Integer> listarEstabelecimentoIdsAtivosPorProfissional(Integer profissionalId) {
        return repository.findByCodProfissionalAndDataFimIsNull(profissionalId)
                .stream().map(e -> e.getCodEstabelecimento()).toList();
    }

    @Override
    public List<Integer> listarIdsPorEstabelecimento(Integer estabelecimentoId) {
        return repository.findByCodEstabelecimento(estabelecimentoId)
                .stream().map(e -> e.getCodigo()).toList();
    }

    @Override
    public Page<ProfissionalVinculo> listarPorFiltros(Integer profissionalId, Integer estabelecimentoId, Pageable pageable) {
        return repository.listarPorFiltros(profissionalId, estabelecimentoId, pageable).map(mapper::toModel);
    }

    @Override
    public boolean existeVinculoAtivo(Integer profissionalId, Integer estabelecimentoId) {
        return repository.existsByCodProfissionalAndCodEstabelecimentoAndDataFimIsNull(profissionalId, estabelecimentoId);
    }

    @Override
    public boolean existeVinculoAtivoPorEstabelecimento(Integer estabelecimentoId) {
        return repository.existsByCodEstabelecimentoAndDataFimIsNull(estabelecimentoId);
    }

    @Override
    public ProfissionalVinculo salvar(ProfissionalVinculo vinculo) {
        return mapper.toModel(repository.save(mapper.toEntity(vinculo)));
    }
}

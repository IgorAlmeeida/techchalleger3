package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendaMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AgendaRepositoryPortImpl implements AgendaRepositoryPort {

    private final AgendaRepository repository;
    private final AgendaMapper mapper;

    @Override
    public Optional<Agenda> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Agenda> listarPorProfissionalVinculoId(Integer profissionalVinculoId) {
        return repository.findByCodProfissionalVinculo(profissionalVinculoId)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public Page<Agenda> listarPorFiltros(Integer profissionalVinculoId, Integer estabelecimentoId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Specification<AgendaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (profissionalVinculoId != null) {
                predicates.add(cb.equal(root.get("codProfissionalVinculo"), profissionalVinculoId));
            }
            if (estabelecimentoId != null) {
                predicates.add(cb.equal(root.get("codEstabelecimento"), estabelecimentoId));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataAgenda"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataAgenda"), dataFim));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(mapper::toModel);
    }

    @Override
    public List<Agenda> listarFuturasPorVinculo(Integer profissionalVinculoId) {
        return repository.findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(profissionalVinculoId, LocalDate.now())
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public boolean existeAgendaFuturaPorVinculo(Integer profissionalVinculoId) {
        return repository.existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(profissionalVinculoId, LocalDate.now());
    }

    @Override
    public boolean existeAgendaPorVinculoEData(Integer profissionalVinculoId, LocalDate dataAgenda) {
        return repository.existsByCodProfissionalVinculoAndDataAgenda(profissionalVinculoId, dataAgenda);
    }

    @Override
    public Agenda salvar(Agenda agenda) {
        return mapper.toModel(repository.save(mapper.toEntity(agenda)));
    }

    @Override
    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.EscalaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EscalaRepositoryPortImpl implements EscalaRepositoryPort {

    private final EscalaRepository repository;
    private final EscalaMapper mapper;

    @Override
    public Optional<Escala> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Escala> listarPorProfissionalVinculoId(Integer profissionalVinculoId) {
        return repository.findByCodProfissionalVinculo(profissionalVinculoId)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Escala> listarPorFiltros(Integer estabelecimentoId, Integer profissionalVinculoId) {
        return repository.listarPorFiltros(estabelecimentoId, profissionalVinculoId)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public Escala salvar(Escala escala) {
        return mapper.toModel(repository.save(mapper.toEntity(escala)));
    }
}

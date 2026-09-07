package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AvaliacaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AvaliacaoRepositoryPortImpl implements AvaliacaoRepositoryPort {

    private final AvaliacaoRepository repository;
    private final AvaliacaoMapper mapper;

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        return mapper.toModel(repository.save(mapper.toEntity(avaliacao)));
    }

    @Override
    public Optional<Avaliacao> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Avaliacao> listarPorEstabelecimentoId(Integer estabelecimentoId) {
        return repository.findByCodEstabelecimento(estabelecimentoId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Avaliacao> listarPorProfissionalVinculoId(Integer profissionalVinculoId) {
        return repository.findByCodProfissionalVinculo(profissionalVinculoId).stream().map(mapper::toModel).toList();
    }

    @Override
    public double calcularNotaMedia(Integer estabelecimentoId) {
        return repository.calcularNotaMedia(estabelecimentoId);
    }

    @Override
    public boolean existePorAgendamentoId(Integer agendamentoId) {
        return repository.existsByCodAgendamento(agendamentoId);
    }
}

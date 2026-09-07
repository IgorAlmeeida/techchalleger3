package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.EstabelecimentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstabelecimentoRepositoryPortImpl implements EstabelecimentoRepositoryPort {

    private final EstabelecimentoRepository repository;
    private final EstabelecimentoMapper mapper;

    @Override
    public Optional<Estabelecimento> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public boolean existePorCnpj(String cnpj) {
        return repository.existsByCnpj(cnpj);
    }

    @Override
    public boolean existePorCnpjExcluindoId(String cnpj, Integer id) {
        return repository.existsByCnpjAndCodigoNot(cnpj, id);
    }

    @Override
    public Page<Estabelecimento> listarAtivos(Pageable pageable) {
        return repository.findAllByAtivo(true, pageable).map(mapper::toModel);
    }

    @Override
    public List<Estabelecimento> listarAtivos() {
        return repository.findAllByAtivo(true).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Estabelecimento> listarPorIds(List<Integer> ids) {
        return repository.findAllByCodigoIn(ids).stream().map(mapper::toModel).toList();
    }

    @Override
    public Page<Estabelecimento> buscarComFiltros(String nome, String localizacao, Integer servicoId,
                                                   BigDecimal precoMin, BigDecimal precoMax,
                                                   Double notaMinima, LocalDate data, Pageable pageable) {
        return repository.buscarComFiltros(nome, localizacao, pageable).map(mapper::toModel);
    }

    @Override
    public Estabelecimento salvar(Estabelecimento estabelecimento) {
        return mapper.toModel(repository.save(mapper.toEntity(estabelecimento)));
    }
}

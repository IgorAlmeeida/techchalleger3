package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServicoRepositoryPortImpl implements ServicoRepositoryPort {

    private final ServicoRepository repository;
    private final ServicoMapper mapper;

    @Override
    public Optional<Servico> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public Page<Servico> listarAtivos(Pageable pageable) {
        return repository.findAllByAtivo(true, pageable).map(mapper::toModel);
    }

    @Override
    public List<Servico> listarAtivos() {
        return repository.findAllByAtivo(true).stream().map(mapper::toModel).toList();
    }

    @Override
    public Servico salvar(Servico servico) {
        return mapper.toModel(repository.save(mapper.toEntity(servico)));
    }
}

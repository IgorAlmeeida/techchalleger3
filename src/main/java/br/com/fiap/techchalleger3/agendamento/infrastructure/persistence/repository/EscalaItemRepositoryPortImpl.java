package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.EscalaItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EscalaItemRepositoryPortImpl implements EscalaItemRepositoryPort {

    private final EscalaItemRepository repository;
    private final EscalaItemMapper mapper;

    @Override
    public List<EscalaItem> listarPorEscalaId(Integer escalaId) {
        return repository.findByCodEscala(escalaId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<EscalaItem> listarAtivosPorEscalaId(Integer escalaId) {
        return repository.findByCodEscalaAndAtivaTrue(escalaId).stream().map(mapper::toModel).toList();
    }

    @Override
    public void deletarPorEscalaId(Integer escalaId) {
        repository.deleteByCodEscala(escalaId);
    }

    @Override
    public EscalaItem salvar(EscalaItem item) {
        return mapper.toModel(repository.save(mapper.toEntity(item)));
    }
}

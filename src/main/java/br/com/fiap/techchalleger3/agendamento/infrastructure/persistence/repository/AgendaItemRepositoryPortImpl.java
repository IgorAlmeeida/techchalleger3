package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendaItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgendaItemRepositoryPortImpl implements AgendaItemRepositoryPort {

    private final AgendaItemRepository repository;
    private final AgendaItemMapper mapper;

    @Override
    public List<AgendaItem> listarPorAgendaId(Integer agendaId) {
        return repository.findByCodAgenda(agendaId).stream().map(mapper::toModel).toList();
    }

    @Override
    public boolean existePorAgendaIdsEServico(List<Integer> agendaIds, Integer servicoId) {
        if (agendaIds == null || agendaIds.isEmpty()) return false;
        return repository.existsByCodAgendaInAndCodServico(agendaIds, servicoId);
    }

    @Override
    public AgendaItem salvar(AgendaItem item) {
        return mapper.toModel(repository.save(mapper.toEntity(item)));
    }
}

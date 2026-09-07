package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;

import java.util.List;

public interface AgendaItemRepositoryPort {
    List<AgendaItem> listarPorAgendaId(Integer agendaId);
    boolean existePorAgendaIdsEServico(List<Integer> agendaIds, Integer servicoId);
    AgendaItem salvar(AgendaItem item);
}

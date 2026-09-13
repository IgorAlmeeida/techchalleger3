package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;

import java.util.List;

/**
 * Porta de saída para persistência de itens de agenda ({@link br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem}).
 */
public interface AgendaItemRepositoryPort {
    List<AgendaItem> listarPorAgendaId(Integer agendaId);
    boolean existePorAgendaIdsEServico(List<Integer> agendaIds, Integer servicoId);
    AgendaItem salvar(AgendaItem item);
    void deletarPorAgendaId(Integer agendaId);
}

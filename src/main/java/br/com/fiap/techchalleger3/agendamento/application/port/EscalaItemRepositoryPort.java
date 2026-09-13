package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;

import java.util.List;

/**
 * Porta de saída para persistência de itens de escala ({@link br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem}).
 */
public interface EscalaItemRepositoryPort {
    List<EscalaItem> listarPorEscalaId(Integer escalaId);
    List<EscalaItem> listarAtivosPorEscalaId(Integer escalaId);
    void deletarPorEscalaId(Integer escalaId);
    EscalaItem salvar(EscalaItem item);
}

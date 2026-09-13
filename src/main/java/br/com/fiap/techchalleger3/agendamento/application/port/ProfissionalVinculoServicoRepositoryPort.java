package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;

import java.util.List;

/**
 * Porta de saída para persistência de associações entre vínculos e serviços ({@link br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico}).
 */
public interface ProfissionalVinculoServicoRepositoryPort {
    List<ProfissionalVinculoServico> listarPorProfissionalVinculoId(Integer profissionalVinculoId);
    boolean existePorVinculoEServico(Integer profissionalVinculoId, Integer servicoId);
    void deletarPorVinculoEServico(Integer profissionalVinculoId, Integer servicoId);
    ProfissionalVinculoServico salvar(ProfissionalVinculoServico vinculoServico);
}

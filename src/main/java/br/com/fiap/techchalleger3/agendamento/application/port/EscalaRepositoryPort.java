package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de escalas ({@link br.com.fiap.techchalleger3.agendamento.domain.model.Escala}).
 */
public interface EscalaRepositoryPort {
    Optional<Escala> buscarPorId(Integer id);
    List<Escala> listarPorProfissionalVinculoId(Integer profissionalVinculoId);
    List<Escala> listarPorFiltros(Integer estabelecimentoId, Integer profissionalVinculoId);
    Escala salvar(Escala escala);
}

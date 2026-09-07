package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;

import java.util.List;
import java.util.Optional;

public interface EscalaRepositoryPort {
    Optional<Escala> buscarPorId(Integer id);
    List<Escala> listarPorProfissionalVinculoId(Integer profissionalVinculoId);
    List<Escala> listarPorFiltros(Integer estabelecimentoId, Integer profissionalVinculoId);
    Escala salvar(Escala escala);
}

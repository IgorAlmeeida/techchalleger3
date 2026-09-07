package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepositoryPort {
    Avaliacao salvar(Avaliacao avaliacao);
    Optional<Avaliacao> buscarPorId(Integer id);
    List<Avaliacao> listarPorEstabelecimentoId(Integer estabelecimentoId);
    List<Avaliacao> listarPorProfissionalVinculoId(Integer profissionalVinculoId);
    double calcularNotaMedia(Integer estabelecimentoId);
    boolean existePorAgendamentoId(Integer agendamentoId);
}

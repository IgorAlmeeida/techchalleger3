package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;

import java.util.Optional;

public interface IntegracaoCalendarioRepositoryPort {
    IntegracaoCalendarioExterno salvar(IntegracaoCalendarioExterno integracao);
    Optional<IntegracaoCalendarioExterno> buscarAtivaByClienteId(Integer clienteId);
    Optional<IntegracaoCalendarioExterno> buscarAtivaByProfissionalId(Integer profissionalId);
    void desativarPorClienteId(Integer clienteId);
    void desativarPorProfissionalId(Integer profissionalId);
}

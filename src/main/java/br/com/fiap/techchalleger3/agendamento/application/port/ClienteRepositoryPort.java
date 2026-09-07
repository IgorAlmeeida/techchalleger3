package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;

import java.util.Optional;

public interface ClienteRepositoryPort {
    Optional<Cliente> buscarPorId(Integer id);
    Optional<Cliente> buscarPorUsuarioId(Integer usuarioId);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorEmail(String email);
    boolean existePorCpf(String cpf);
    Cliente salvar(Cliente cliente);
}

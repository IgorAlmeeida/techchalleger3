package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> buscarPorCodKeycloak(String codKeycloak);
    Optional<Usuario> buscarPorId(Integer id);
    Usuario salvar(Usuario usuario);
}

package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;

import java.util.Optional;

/**
 * Porta de saída para persistência de usuários.
 */
public interface UsuarioRepositoryPort {
    Optional<Usuario> buscarPorUuid(String uuid);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(Integer id);
    Usuario salvar(Usuario usuario);
    void atualizarSenha(String uuid, String novaSenhaHash);
    void deletar(Integer id);
}

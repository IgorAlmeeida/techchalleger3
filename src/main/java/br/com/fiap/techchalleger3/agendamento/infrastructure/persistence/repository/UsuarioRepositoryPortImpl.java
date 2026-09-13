package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryPortImpl implements UsuarioRepositoryPort {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    @Override
    public Optional<Usuario> buscarPorUuid(String uuid) {
        return repository.findByUuid(uuid).map(mapper::toModel);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.toModel(repository.save(mapper.toEntity(usuario)));
    }

    @Override
    @Transactional
    public void atualizarSenha(String uuid, String novaSenhaHash) {
        repository.updateSenhaHash(uuid, novaSenhaHash);
    }

    @Override
    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}

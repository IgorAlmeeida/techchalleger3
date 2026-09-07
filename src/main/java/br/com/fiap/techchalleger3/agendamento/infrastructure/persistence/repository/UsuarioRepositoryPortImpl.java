package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryPortImpl implements UsuarioRepositoryPort {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    @Override
    public Optional<Usuario> buscarPorCodKeycloak(String codKeycloak) {
        return repository.findByCodKeycloak(codKeycloak).map(mapper::toModel);
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.toModel(repository.save(mapper.toEntity(usuario)));
    }
}

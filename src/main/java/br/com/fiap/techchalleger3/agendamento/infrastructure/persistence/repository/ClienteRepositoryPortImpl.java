package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryPortImpl implements ClienteRepositoryPort {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<Cliente> buscarPorUsuarioId(Integer usuarioId) {
        return repository.findByCodUsuario(usuarioId).map(mapper::toModel);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf).map(mapper::toModel);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return repository.existsByCpf(cpf);
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return mapper.toModel(repository.save(mapper.toEntity(cliente)));
    }
}

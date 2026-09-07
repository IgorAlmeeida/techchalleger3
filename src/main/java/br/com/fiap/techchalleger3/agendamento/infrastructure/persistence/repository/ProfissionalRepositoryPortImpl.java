package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ProfissionalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfissionalRepositoryPortImpl implements ProfissionalRepositoryPort {

    private final ProfissionalRepository repository;
    private final ProfissionalMapper mapper;

    @Override
    public Optional<Profissional> buscarPorId(Integer id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<Profissional> buscarPorUsuarioId(Integer usuarioId) {
        return repository.findByCodUsuario(usuarioId).map(mapper::toModel);
    }

    @Override
    public Optional<Profissional> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public List<Profissional> listarTodos() {
        return repository.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Page<Profissional> listarComFiltros(String nome, String especialidades, boolean incluirInativos, Pageable pageable) {
        return repository.buscarComFiltros(nome, especialidades, incluirInativos, pageable).map(mapper::toModel);
    }

    @Override
    public Profissional salvar(Profissional profissional) {
        return mapper.toModel(repository.save(mapper.toEntity(profissional)));
    }
}

package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepositoryPort {
    Optional<Profissional> buscarPorId(Integer id);
    Optional<Profissional> buscarPorUsuarioId(Integer usuarioId);
    Optional<Profissional> buscarPorEmail(String email);
    List<Profissional> listarTodos();
    Page<Profissional> listarComFiltros(String nome, String especialidade, boolean incluirInativos, Pageable pageable);
    Profissional salvar(Profissional profissional);
}

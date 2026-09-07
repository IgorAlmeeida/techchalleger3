package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ServicoRepositoryPort {
    Optional<Servico> buscarPorId(Integer id);
    Page<Servico> listarAtivos(Pageable pageable);
    List<Servico> listarAtivos();
    Servico salvar(Servico servico);
}

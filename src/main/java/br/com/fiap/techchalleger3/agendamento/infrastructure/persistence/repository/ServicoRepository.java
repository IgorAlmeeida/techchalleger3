package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<ServicoEntity, Integer> {
    Page<ServicoEntity> findAllByAtivo(Boolean ativo, Pageable pageable);
    List<ServicoEntity> findAllByAtivo(Boolean ativo);
}

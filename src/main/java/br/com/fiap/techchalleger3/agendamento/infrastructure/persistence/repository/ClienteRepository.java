package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodUsuario(Integer codUsuario);
    Optional<ClienteEntity> findByCpf(String cpf);
    Optional<ClienteEntity> findByEmail(String email);
    boolean existsByCpf(String cpf);
}

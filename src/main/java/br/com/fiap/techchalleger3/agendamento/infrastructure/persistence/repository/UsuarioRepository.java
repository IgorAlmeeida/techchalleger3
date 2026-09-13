package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByUuid(String uuid);
    Optional<UsuarioEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE UsuarioEntity u SET u.senhaHash = :hash WHERE u.uuid = :uuid")
    void updateSenhaHash(@Param("uuid") String uuid, @Param("hash") String hash);
}

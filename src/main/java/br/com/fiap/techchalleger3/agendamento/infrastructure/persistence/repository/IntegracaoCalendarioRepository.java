package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.IntegracaoCalendarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IntegracaoCalendarioRepository extends JpaRepository<IntegracaoCalendarioEntity, Integer> {

    Optional<IntegracaoCalendarioEntity> findByCodClienteAndAtivoTrue(Integer codCliente);

    Optional<IntegracaoCalendarioEntity> findByCodProfissionalAndAtivoTrue(Integer codProfissional);

    @Modifying
    @Query("UPDATE IntegracaoCalendarioEntity i SET i.ativo = false WHERE i.codCliente = :clienteId")
    void desativarPorCodCliente(Integer clienteId);

    @Modifying
    @Query("UPDATE IntegracaoCalendarioEntity i SET i.ativo = false WHERE i.codProfissional = :profissionalId")
    void desativarPorCodProfissional(Integer profissionalId);
}

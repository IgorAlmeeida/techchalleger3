package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.RepositoryTestBase;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRepositoryTest extends RepositoryTestBase {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UsuarioRepository repository;

    @Test
    void devebuscarPorUuid_quandoExiste() {
        em.persist(UsuarioEntity.builder().uuid("kc-abc").email("a@test.com").nome("A").senhaHash("h").role(RoleEnum.CLIENTE).build());
        em.flush();

        Optional<UsuarioEntity> result = repository.findByUuid("kc-abc");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("kc-abc");
    }

    @Test
    void deveRetornarVazio_quandoCodNaoExiste() {
        Optional<UsuarioEntity> result = repository.findByUuid("inexistente");
        assertThat(result).isEmpty();
    }
}

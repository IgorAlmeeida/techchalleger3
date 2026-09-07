package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.RepositoryTestBase;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EstabelecimentoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class EstabelecimentoRepositoryTest extends RepositoryTestBase {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EstabelecimentoRepository repository;

    @BeforeEach
    void setUp() {
        em.persist(EstabelecimentoEntity.builder()
                .nome("Studio Bela Arte").cnpj("11.111.111/0001-11")
                .endereco("Rua das Flores, São Paulo").build());
        em.persist(EstabelecimentoEntity.builder()
                .nome("Salão Moderno").cnpj("22.222.222/0001-22")
                .endereco("Av. Paulista, São Paulo").build());
        em.persist(EstabelecimentoEntity.builder()
                .nome("Inativo Beauty").cnpj("33.333.333/0001-33")
                .ativo(false).build());
        em.flush();
    }

    @Test
    void deveBuscarPorNome_quandoFiltroNomeInformado() {
        Page<EstabelecimentoEntity> result = repository.buscarComFiltros("studio", null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Studio Bela Arte");
    }

    @Test
    void deveBuscarPorLocalizacao_quandoFiltroLocalizacaoInformado() {
        Page<EstabelecimentoEntity> result = repository.buscarComFiltros(null, "paulista", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Salão Moderno");
    }

    @Test
    void deveRetornarSomenteAtivos_quandoSemFiltros() {
        Page<EstabelecimentoEntity> result = repository.buscarComFiltros(null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).noneMatch(e -> !e.getAtivo());
    }
}

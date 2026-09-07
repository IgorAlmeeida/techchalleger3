package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.RepositoryTestBase;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AvaliacaoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ClienteEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EstabelecimentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoRepositoryTest extends RepositoryTestBase {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AvaliacaoRepository repository;

    private Integer codAgendamento;
    private Integer codCliente;
    private Integer codEstabelecimento;
    private Integer codVinculo;

    @BeforeEach
    void setUp() {
        UsuarioEntity usuCliente = em.persist(UsuarioEntity.builder().codKeycloak("kc-cli-av").role(RoleEnum.CLIENTE).build());
        UsuarioEntity usuProf = em.persist(UsuarioEntity.builder().codKeycloak("kc-prof-av").role(RoleEnum.PROFISSIONAL).build());
        EstabelecimentoEntity estab = em.persist(EstabelecimentoEntity.builder()
                .nome("Studio Av").cnpj("44.444.444/0001-44").build());
        ProfissionalEntity prof = em.persist(ProfissionalEntity.builder()
                .codUsuario(usuProf.getCodigo()).nome("Carlos").build());
        ClienteEntity cliente = em.persist(ClienteEntity.builder()
                .codUsuario(usuCliente.getCodigo()).nome("Ana").cpf("222.222.222-22").build());
        ProfissionalVinculoEntity vinculo = em.persist(ProfissionalVinculoEntity.builder()
                .codProfissional(prof.getCodigo()).codEstabelecimento(estab.getCodigo())
                .dataInicio(LocalDate.of(2026, 1, 1)).build());
        EscalaEntity escala = em.persist(EscalaEntity.builder()
                .codProfissionalVinculo(vinculo.getCodigo()).codEstabelecimento(estab.getCodigo())
                .diaSemana(DiaSemanaEnum.SEXTA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(18, 0)).build());
        AgendaEntity agenda = em.persist(AgendaEntity.builder()
                .codEscala(escala.getCodigo()).dataAgenda(LocalDate.of(2026, 9, 11))
                .diaSemana(DiaSemanaEnum.SEXTA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(18, 0))
                .codEstabelecimento(estab.getCodigo()).codProfissionalVinculo(vinculo.getCodigo()).build());
        AgendamentoEntity agendamento = em.persist(AgendamentoEntity.builder()
                .codAgenda(agenda.getCodigo()).codCliente(cliente.getCodigo())
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 30))
                .status(StatusAgendamentoEnum.REALIZADO).presencaConfirmada(true).build());
        em.flush();

        codAgendamento = agendamento.getCodigo();
        codCliente = cliente.getCodigo();
        codEstabelecimento = estab.getCodigo();
        codVinculo = vinculo.getCodigo();
    }

    @Test
    void deveCalcularNotaMedia_quandoHaAvaliacoes() {
        em.persist(AvaliacaoEntity.builder()
                .codAgendamento(codAgendamento).codCliente(codCliente)
                .codEstabelecimento(codEstabelecimento).codProfissionalVinculo(codVinculo)
                .nota(4).build());
        em.flush();

        double media = repository.calcularNotaMedia(codEstabelecimento);
        assertThat(media).isEqualTo(4.0);
    }

    @Test
    void deveRetornarZero_quandoSemAvaliacoes() {
        double media = repository.calcularNotaMedia(codEstabelecimento);
        assertThat(media).isEqualTo(0.0);
    }

    @Test
    void deveConfirmar_quandoAvaliacaoExisteParaAgendamento() {
        em.persist(AvaliacaoEntity.builder()
                .codAgendamento(codAgendamento).codCliente(codCliente)
                .codEstabelecimento(codEstabelecimento).codProfissionalVinculo(codVinculo)
                .nota(5).build());
        em.flush();

        assertThat(repository.existsByCodAgendamento(codAgendamento)).isTrue();
    }

    @Test
    void deveNegar_quandoAvaliacaoNaoExisteParaAgendamento() {
        assertThat(repository.existsByCodAgendamento(9999)).isFalse();
    }
}

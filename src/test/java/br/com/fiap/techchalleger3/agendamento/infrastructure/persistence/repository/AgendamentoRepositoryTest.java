package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.RepositoryTestBase;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ClienteEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EstabelecimentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ServicoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoRepositoryTest extends RepositoryTestBase {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AgendamentoRepository repository;

    private Integer codCliente;
    private Integer codVinculo;
    private Integer codServico;

    @BeforeEach
    void setUp() {
        UsuarioEntity usuCliente = em.persist(UsuarioEntity.builder().codKeycloak("kc-cli").role(RoleEnum.CLIENTE).build());
        UsuarioEntity usuProf = em.persist(UsuarioEntity.builder().codKeycloak("kc-prof").role(RoleEnum.PROFISSIONAL).build());
        EstabelecimentoEntity estab = em.persist(EstabelecimentoEntity.builder()
                .nome("Studio Test").cnpj("00.000.000/0001-00").build());
        ProfissionalEntity prof = em.persist(ProfissionalEntity.builder()
                .codUsuario(usuProf.getCodigo()).nome("Ana").build());
        ClienteEntity cliente = em.persist(ClienteEntity.builder()
                .codUsuario(usuCliente.getCodigo()).nome("Maria").cpf("111.111.111-11").build());
        ProfissionalVinculoEntity vinculo = em.persist(ProfissionalVinculoEntity.builder()
                .codProfissional(prof.getCodigo()).codEstabelecimento(estab.getCodigo())
                .dataInicio(LocalDate.of(2026, 1, 1)).build());
        EscalaEntity escala = em.persist(EscalaEntity.builder()
                .codProfissionalVinculo(vinculo.getCodigo()).codEstabelecimento(estab.getCodigo())
                .diaSemana(DiaSemanaEnum.QUARTA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(18, 0)).build());
        AgendaEntity agenda = em.persist(AgendaEntity.builder()
                .codEscala(escala.getCodigo()).dataAgenda(LocalDate.of(2026, 9, 10))
                .diaSemana(DiaSemanaEnum.QUARTA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(18, 0))
                .codEstabelecimento(estab.getCodigo()).codProfissionalVinculo(vinculo.getCodigo()).build());
        ServicoEntity servico = em.persist(ServicoEntity.builder()
                .nome("Corte").duracaoMinutos(30).ativo(true).build());
        em.persist(AgendamentoEntity.builder()
                .codAgenda(agenda.getCodigo()).codCliente(cliente.getCodigo()).codServico(servico.getCodigo())
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 30))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build());
        em.flush();

        codCliente = cliente.getCodigo();
        codVinculo = vinculo.getCodigo();
        codServico = servico.getCodigo();
    }

    @Test
    void deveBuscarAgendamentosPaisDoCliente_quandoDentroDoFiltroDeData() {
        List<AgendamentoEntity> result = repository.buscarPaisDoPaciente(
                codCliente,
                List.of(StatusAgendamentoEnum.AGENDADO),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodCliente()).isEqualTo(codCliente);
    }

    @Test
    void deveRetornarVazio_quandoClienteSemAgendamentos() {
        List<AgendamentoEntity> result = repository.buscarPaisDoPaciente(
                9999, List.of(StatusAgendamentoEnum.AGENDADO), null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void deveConfirmar_quandoAgendadoExistePorClienteVinculoServico() {
        boolean existe = repository.existeAgendadoPorClienteVinculoServico(
                codCliente, codVinculo, codServico, StatusAgendamentoEnum.AGENDADO);
        assertThat(existe).isTrue();
    }

    @Test
    void deveNegar_quandoClienteNaoTemAgendamentoPorVinculoServico() {
        boolean existe = repository.existeAgendadoPorClienteVinculoServico(
                9999, codVinculo, codServico, StatusAgendamentoEnum.AGENDADO);
        assertThat(existe).isFalse();
    }
}

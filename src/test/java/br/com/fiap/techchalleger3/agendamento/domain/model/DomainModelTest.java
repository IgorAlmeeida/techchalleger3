package br.com.fiap.techchalleger3.agendamento.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTest {

    @Test
    void agendamento_isDisponivel_retornaTrue_quandoStatusDisponivel() {
        Agendamento ag = Agendamento.builder().status(StatusAgendamentoEnum.DISPONIVEL).build();
        assertThat(ag.isDisponivel()).isTrue();
    }

    @Test
    void agendamento_isDisponivel_retornaFalse_quandoOutroStatus() {
        Agendamento ag = Agendamento.builder().status(StatusAgendamentoEnum.AGENDADO).build();
        assertThat(ag.isDisponivel()).isFalse();
    }

    @Test
    void agendamento_noArgsConstructor() {
        Agendamento ag = new Agendamento();
        assertThat(ag.getStatus()).isNull();
    }

    @Test
    void profissionalVinculo_isAtivo_retornaTrue_quandoDataFimNula() {
        ProfissionalVinculo v = ProfissionalVinculo.builder().id(1).dataFim(null).build();
        assertThat(v.isAtivo()).isTrue();
    }

    @Test
    void profissionalVinculo_isAtivo_retornaFalse_quandoDataFimDefinida() {
        ProfissionalVinculo v = ProfissionalVinculo.builder().id(1).dataFim(LocalDate.now()).build();
        assertThat(v.isAtivo()).isFalse();
    }

    @Test
    void profissionalVinculo_noArgsConstructor() {
        ProfissionalVinculo v = new ProfissionalVinculo();
        assertThat(v.getId()).isNull();
    }

    @Test
    void profissional_defaultAtivo_verdadeiro() {
        Profissional p = Profissional.builder().id(1).nome("Dr.").build();
        assertThat(p.getAtivo()).isTrue();
    }

    @Test
    void profissional_noArgsConstructor() {
        Profissional p = new Profissional();
        assertThat(p.getNome()).isNull();
    }

    @Test
    void avaliacao_builderENoArgs() {
        Avaliacao av = Avaliacao.builder().id(1).nota(5).comentario("Ótimo!").build();
        assertThat(av.getNota()).isEqualTo(5);
        Avaliacao av2 = new Avaliacao();
        assertThat(av2.getId()).isNull();
    }

    @Test
    void cliente_noArgsConstructor() {
        Cliente c = new Cliente();
        assertThat(c.getNome()).isNull();
    }

    @Test
    void estabelecimento_noArgsConstructor() {
        Estabelecimento e = new Estabelecimento();
        assertThat(e.getNome()).isNull();
    }

    @Test
    void escala_noArgsConstructor() {
        Escala e = new Escala();
        assertThat(e.getId()).isNull();
    }

    @Test
    void escalaItem_builderENoArgs() {
        EscalaItem ei = EscalaItem.builder().id(1).servicoId(5).build();
        assertThat(ei.getServicoId()).isEqualTo(5);
        EscalaItem ei2 = new EscalaItem();
        assertThat(ei2.getId()).isNull();
    }

    @Test
    void agenda_noArgsConstructor() {
        Agenda ag = new Agenda();
        assertThat(ag.getId()).isNull();
    }

    @Test
    void agendaItem_builderENoArgs() {
        AgendaItem ai = AgendaItem.builder().id(1).servicoId(3).build();
        assertThat(ai.getServicoId()).isEqualTo(3);
        AgendaItem ai2 = new AgendaItem();
        assertThat(ai2.getId()).isNull();
    }

    @Test
    void usuario_noArgsConstructor() {
        Usuario u = new Usuario();
        assertThat(u.getId()).isNull();
    }

    @Test
    void servico_noArgsConstructor() {
        Servico s = new Servico();
        assertThat(s.getNome()).isNull();
    }

    @Test
    void profissionalVinculoServico_noArgsConstructor() {
        ProfissionalVinculoServico pvs = new ProfissionalVinculoServico();
        assertThat(pvs.getId()).isNull();
    }

    @Test
    void roleEnum_valores() {
        assertThat(RoleEnum.values()).contains(RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.PROFISSIONAL);
    }

    @Test
    void statusAgendamentoEnum_valores() {
        assertThat(StatusAgendamentoEnum.values()).hasSizeGreaterThan(0);
    }

    @Test
    void diaSemanaEnum_valores() {
        assertThat(DiaSemanaEnum.values()).contains(DiaSemanaEnum.SEGUNDA, DiaSemanaEnum.SABADO);
    }

    @Test
    void agendamento_setters() {
        Agendamento ag = new Agendamento();
        ag.setId(1);
        ag.setAgendaId(2);
        ag.setAgendamentoPaiId(null);
        ag.setServicoId(3);
        ag.setHoraInicio(LocalTime.of(9, 0));
        ag.setHoraFim(LocalTime.of(10, 0));
        ag.setClienteId(5);
        ag.setStatus(StatusAgendamentoEnum.AGENDADO);
        ag.setPresencaConfirmada(true);
        ag.setDhInsert(LocalDateTime.now());
        ag.setDhAtualizacao(LocalDateTime.now());
        ag.setDataAgenda(LocalDate.now());
        assertThat(ag.getId()).isEqualTo(1);
        assertThat(ag.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
    }

    @Test
    void agenda_setters() {
        Agenda ag = new Agenda();
        ag.setId(1);
        ag.setEscalaId(2);
        ag.setDataAgenda(LocalDate.now());
        ag.setDiaSemana(DiaSemanaEnum.SEGUNDA);
        ag.setHoraInicio(LocalTime.of(9, 0));
        ag.setHoraFim(LocalTime.of(17, 0));
        ag.setEstabelecimentoId(3);
        ag.setProfissionalVinculoId(4);
        ag.setDhInsert(LocalDateTime.now());
        ag.setDhAtualizacao(LocalDateTime.now());
        assertThat(ag.getId()).isEqualTo(1);
    }

    @Test
    void escala_setters() {
        Escala e = new Escala();
        e.setId(1);
        e.setProfissionalVinculoId(2);
        e.setEstabelecimentoId(3);
        e.setDiaSemana(DiaSemanaEnum.TERCA);
        e.setHoraInicio(LocalTime.of(8, 0));
        e.setHoraFim(LocalTime.of(12, 0));
        e.setDhInsert(LocalDateTime.now());
        e.setDhAtualizacao(LocalDateTime.now());
        assertThat(e.getId()).isEqualTo(1);
    }

    @Test
    void cliente_setters() {
        Cliente c = new Cliente();
        c.setId(1);
        c.setUsuarioId(2);
        c.setNome("João");
        c.setEmail("joao@x.com");
        c.setCpf("111");
        c.setDataNascimento(LocalDate.of(1990, 1, 1));
        c.setTelefone("11999");
        c.setSexo("M");
        c.setEndereco("Rua A");
        c.setDhInsert(LocalDateTime.now());
        c.setDhAtualizacao(LocalDateTime.now());
        assertThat(c.getNome()).isEqualTo("João");
    }

    @Test
    void profissional_setters() {
        Profissional p = new Profissional();
        p.setId(1);
        p.setUsuarioId(2);
        p.setNome("Dr.");
        p.setEmail("dr@x.com");
        p.setEspecialidades(List.of("Corte"));
        p.setEndereco("Rua B");
        p.setAtivo(false);
        p.setDhInsert(LocalDateTime.now());
        p.setDhAtualizacao(LocalDateTime.now());
        assertThat(p.getAtivo()).isFalse();
    }

    @Test
    void estabelecimento_setters() {
        Estabelecimento e = new Estabelecimento();
        e.setId(1);
        e.setNome("Studio");
        e.setCnpj("00.0");
        e.setEndereco("Rua");
        e.setTelefone("11999");
        e.setResponsavelNome("Ana");
        e.setResponsavelCpf("111");
        e.setFotosUrls(List.of("http://x.com/img.jpg"));
        e.setAtivo(true);
        e.setDhInsert(LocalDateTime.now());
        e.setDhAtualizacao(LocalDateTime.now());
        assertThat(e.getNome()).isEqualTo("Studio");
    }

    @Test
    void profissionalVinculo_setters() {
        ProfissionalVinculo v = new ProfissionalVinculo();
        v.setId(1);
        v.setProfissionalId(2);
        v.setEstabelecimentoId(3);
        v.setDataInicio(LocalDate.now());
        v.setDataFim(LocalDate.now().plusMonths(1));
        v.setDhInsert(LocalDateTime.now());
        v.setDhAtualizacao(LocalDateTime.now());
        assertThat(v.isAtivo()).isFalse();
    }

    @Test
    void usuario_setters() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setKeycloakId("kc-1");
        u.setRole(RoleEnum.CLIENTE);
        assertThat(u.getRole()).isEqualTo(RoleEnum.CLIENTE);
    }

    @Test
    void servico_setters() {
        Servico s = new Servico();
        s.setId(1);
        s.setNome("Corte");
        s.setDuracaoMinutos(30);
        s.setAtivo(true);
        assertThat(s.getNome()).isEqualTo("Corte");
    }

    @Test
    void avaliacao_setters() {
        Avaliacao av = new Avaliacao();
        av.setId(1);
        av.setAgendamentoId(2);
        av.setClienteId(3);
        av.setEstabelecimentoId(4);
        av.setProfissionalVinculoId(5);
        av.setNota(5);
        av.setComentario("Ótimo!");
        av.setDhInsert(LocalDateTime.now());
        assertThat(av.getNota()).isEqualTo(5);
    }

    @Test
    void agendamento_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Agendamento ag = Agendamento.builder()
                .id(1).agendaId(2).dataAgenda(LocalDate.now()).agendamentoPaiId(3)
                .servicoId(4).horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .clienteId(5).status(StatusAgendamentoEnum.CANCELADO)
                .presencaConfirmada(true).dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(ag.getAgendamentoPaiId()).isEqualTo(3);
        assertThat(ag.getDataAgenda()).isNotNull();
        assertThat(ag.getDhInsert()).isNotNull();
    }

    @Test
    void agenda_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Agenda ag = Agenda.builder()
                .id(1).escalaId(2).dataAgenda(LocalDate.now()).diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(17, 0))
                .estabelecimentoId(3).profissionalVinculoId(4)
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(ag.getEscalaId()).isEqualTo(2);
        assertThat(ag.getEstabelecimentoId()).isEqualTo(3);
    }

    @Test
    void escala_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Escala e = Escala.builder()
                .id(1).profissionalVinculoId(2).estabelecimentoId(3)
                .diaSemana(DiaSemanaEnum.QUARTA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(16, 0))
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(e.getEstabelecimentoId()).isEqualTo(3);
        assertThat(e.getDhInsert()).isNotNull();
    }

    @Test
    void cliente_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Cliente c = Cliente.builder()
                .id(1).usuarioId(2).nome("João").email("joao@x.com").cpf("111.111.111-11")
                .dataNascimento(LocalDate.of(1990, 1, 1)).telefone("11999991111").sexo("M")
                .endereco("Rua A, 1").dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(c.getCpf()).isEqualTo("111.111.111-11");
        assertThat(c.getDataNascimento()).isNotNull();
    }

    @Test
    void profissional_builderComAtivoFalse() {
        LocalDateTime now = LocalDateTime.now();
        Profissional p = Profissional.builder()
                .id(1).usuarioId(2).nome("Dr.").email("dr@x.com")
                .especialidades(List.of("Corte")).endereco("Rua B")
                .ativo(false).dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(p.getAtivo()).isFalse();
        assertThat(p.getEmail()).isEqualTo("dr@x.com");
    }

    @Test
    void estabelecimento_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Estabelecimento e = Estabelecimento.builder()
                .id(1).nome("Studio X").cnpj("00.000.000/0001-01").endereco("Av. 1")
                .telefone("11000001111").responsavelNome("Ana").responsavelCpf("222.222.222-22")
                .fotosUrls(List.of("http://x.com/img.jpg")).ativo(true)
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(e.getCnpj()).isEqualTo("00.000.000/0001-01");
        assertThat(e.getResponsavelCpf()).isEqualTo("222.222.222-22");
    }

    @Test
    void profissionalVinculo_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        ProfissionalVinculo v = ProfissionalVinculo.builder()
                .id(1).profissionalId(2).estabelecimentoId(3)
                .dataInicio(LocalDate.now()).dataFim(LocalDate.now().plusYears(1))
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(v.getEstabelecimentoId()).isEqualTo(3);
        assertThat(v.getDhInsert()).isNotNull();
    }

    @Test
    void profissionalVinculoServico_builderCompleto() {
        ProfissionalVinculoServico pvs = ProfissionalVinculoServico.builder()
                .id(1).profissionalVinculoId(2).servicoId(3).tarifa(BigDecimal.TEN)
                .build();
        assertThat(pvs.getTarifa()).isEqualTo(BigDecimal.TEN);
        assertThat(pvs.getServicoId()).isEqualTo(3);
    }

    @Test
    void escalaItem_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        EscalaItem ei = EscalaItem.builder()
                .id(1).escalaId(2).servicoId(3).ativa(true)
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(ei.getEscalaId()).isEqualTo(2);
        assertThat(ei.getAtiva()).isTrue();
    }

    @Test
    void agendaItem_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        AgendaItem ai = AgendaItem.builder()
                .id(1).agendaId(2).servicoId(3)
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(ai.getAgendaId()).isEqualTo(2);
        assertThat(ai.getDhInsert()).isNotNull();
    }

    @Test
    void usuario_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Usuario u = Usuario.builder()
                .id(1).keycloakId("kc-uuid-1").role(RoleEnum.PROFISSIONAL)
                .dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(u.getRole()).isEqualTo(RoleEnum.PROFISSIONAL);
        assertThat(u.getKeycloakId()).isEqualTo("kc-uuid-1");
    }

    @Test
    void servico_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Servico s = Servico.builder()
                .id(1).nome("Corte").duracaoMinutos(30).preco(BigDecimal.valueOf(50))
                .ativo(false).dhInsert(now).dhAtualizacao(now)
                .build();
        assertThat(s.getPreco()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(s.getAtivo()).isFalse();
    }

    @Test
    void avaliacao_builderCompleto() {
        LocalDateTime now = LocalDateTime.now();
        Avaliacao a = Avaliacao.builder()
                .id(1).agendamentoId(2).clienteId(3).estabelecimentoId(4)
                .profissionalVinculoId(5).nota(5).comentario("Ótimo!").dhInsert(now)
                .build();
        assertThat(a.getComentario()).isEqualTo("Ótimo!");
        assertThat(a.getProfissionalVinculoId()).isEqualTo(5);
    }

    @Test
    void avaliacao_equalsHashCodeToString() {
        LocalDateTime now = LocalDateTime.now();
        Avaliacao a = Avaliacao.builder().id(1).nota(5).comentario("X").dhInsert(now).build();
        Avaliacao b = Avaliacao.builder().id(1).nota(5).comentario("X").dhInsert(now).build();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).contains("Avaliacao");

        Avaliacao c = Avaliacao.builder().id(2).nota(3).comentario("Y").dhInsert(now).build();
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("not an avaliacao");
    }

    @Test
    void diaSemanaEnum_obterPorCodigo() {
        assertThat(DiaSemanaEnum.obterPorCodigo("SEGUNDA")).isEqualTo(DiaSemanaEnum.SEGUNDA);
        assertThat(DiaSemanaEnum.obterPorCodigo("DOMINGO")).isEqualTo(DiaSemanaEnum.DOMINGO);
    }

    @Test
    void statusAgendamentoEnum_obterPorCodigo() {
        assertThat(StatusAgendamentoEnum.obterPorCodigo("DISPONIVEL")).isEqualTo(StatusAgendamentoEnum.DISPONIVEL);
        assertThat(StatusAgendamentoEnum.obterPorCodigo("RESERVADO")).isEqualTo(StatusAgendamentoEnum.RESERVADO);
    }
}

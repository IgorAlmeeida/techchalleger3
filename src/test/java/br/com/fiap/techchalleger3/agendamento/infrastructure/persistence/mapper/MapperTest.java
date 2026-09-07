package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    // ── AgendamentoMapper ──────────────────────────────────────────────────

    private final AgendamentoMapper agendamentoMapper = new AgendamentoMapperImpl();

    @Test
    void agendamento_toModel_null() {
        assertThat(agendamentoMapper.toModel(null)).isNull();
    }

    @Test
    void agendamento_toEntity_null() {
        assertThat(agendamentoMapper.toEntity(null)).isNull();
    }

    @Test
    void agendamento_toModel() {
        AgendamentoEntity e = AgendamentoEntity.builder()
                .codigo(1).codAgenda(2).codAgendamentoPai(3).codServico(4).codCliente(5)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(true)
                .dhInsert(LocalDateTime.now()).dhAtualizacao(LocalDateTime.now()).build();

        Agendamento m = agendamentoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getAgendaId()).isEqualTo(2);
        assertThat(m.getAgendamentoPaiId()).isEqualTo(3);
        assertThat(m.getServicoId()).isEqualTo(4);
        assertThat(m.getClienteId()).isEqualTo(5);
        assertThat(m.getStatus()).isEqualTo(StatusAgendamentoEnum.AGENDADO);
        assertThat(m.getPresencaConfirmada()).isTrue();
    }

    @Test
    void agendamento_toEntity() {
        Agendamento m = Agendamento.builder()
                .id(10).agendaId(20).agendamentoPaiId(30).servicoId(40).clienteId(50)
                .horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(9, 0))
                .status(StatusAgendamentoEnum.DISPONIVEL).presencaConfirmada(false).build();

        AgendamentoEntity e = agendamentoMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(10);
        assertThat(e.getCodAgenda()).isEqualTo(20);
        assertThat(e.getCodAgendamentoPai()).isEqualTo(30);
        assertThat(e.getCodServico()).isEqualTo(40);
        assertThat(e.getCodCliente()).isEqualTo(50);
        assertThat(e.getStatus()).isEqualTo(StatusAgendamentoEnum.DISPONIVEL);
        assertThat(e.getPresencaConfirmada()).isFalse();
    }

    // ── ClienteMapper ──────────────────────────────────────────────────────

    private final ClienteMapper clienteMapper = new ClienteMapperImpl();

    @Test
    void cliente_toModel_null() {
        assertThat(clienteMapper.toModel(null)).isNull();
    }

    @Test
    void cliente_toModel() {
        ClienteEntity e = ClienteEntity.builder()
                .codigo(1).codUsuario(5).nome("Ana").email("ana@x.com").cpf("111.222.333-44")
                .dataNascimento(LocalDate.of(1990, 1, 1)).telefone("11999").sexo("F").endereco("Rua A").build();

        Cliente m = clienteMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getUsuarioId()).isEqualTo(5);
        assertThat(m.getNome()).isEqualTo("Ana");
    }

    @Test
    void cliente_toEntity() {
        Cliente m = Cliente.builder().id(2).usuarioId(9).nome("Bob").email("bob@x.com").cpf("444.555.666-77").build();

        ClienteEntity e = clienteMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getCodUsuario()).isEqualTo(9);
        assertThat(e.getNome()).isEqualTo("Bob");
    }

    // ── UsuarioMapper ──────────────────────────────────────────────────────

    private final UsuarioMapper usuarioMapper = new UsuarioMapperImpl();

    @Test
    void usuario_toModel_null() {
        assertThat(usuarioMapper.toModel(null)).isNull();
    }

    @Test
    void usuario_toModel() {
        UsuarioEntity e = UsuarioEntity.builder()
                .codigo(1).codKeycloak("kc-uuid").role(RoleEnum.CLIENTE).build();

        Usuario m = usuarioMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getKeycloakId()).isEqualTo("kc-uuid");
    }

    @Test
    void usuario_toEntity() {
        Usuario m = Usuario.builder().id(3).keycloakId("kc-456").role(RoleEnum.PROFISSIONAL).build();

        UsuarioEntity e = usuarioMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(3);
        assertThat(e.getCodKeycloak()).isEqualTo("kc-456");
    }

    // ── ProfissionalMapper ────────────────────────────────────────────────

    private final ProfissionalMapper profissionalMapper = new ProfissionalMapperImpl();

    @Test
    void profissional_toModel_null() {
        assertThat(profissionalMapper.toModel(null)).isNull();
    }

    @Test
    void profissional_toModel() {
        ProfissionalEntity e = ProfissionalEntity.builder()
                .codigo(1).codUsuario(10).nome("Dr. X").email("dx@x.com")
                .especialidades("Corte,Barba").endereco("Rua B").ativo(true).build();

        Profissional m = profissionalMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getUsuarioId()).isEqualTo(10);
        assertThat(m.getNome()).isEqualTo("Dr. X");
        assertThat(m.getEspecialidades()).containsExactly("Corte", "Barba");
    }

    @Test
    void profissional_toEntity() {
        Profissional m = Profissional.builder().id(2).usuarioId(20).nome("Dra. Y")
                .especialidades(List.of("Coloracao")).ativo(false).build();

        ProfissionalEntity e = profissionalMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getEspecialidades()).isEqualTo("Coloracao");
        assertThat(e.getAtivo()).isFalse();
    }

    @Test
    void profissional_stringToList_null() {
        assertThat(profissionalMapper.stringToList(null)).isEmpty();
    }

    @Test
    void profissional_stringToList_blank() {
        assertThat(profissionalMapper.stringToList("  ")).isEmpty();
    }

    @Test
    void profissional_listToString_null() {
        assertThat(profissionalMapper.listToString(null)).isNull();
    }

    @Test
    void profissional_listToString_empty() {
        assertThat(profissionalMapper.listToString(List.of())).isNull();
    }

    @Test
    void profissional_listToString_values() {
        assertThat(profissionalMapper.listToString(List.of("A", "B"))).isEqualTo("A,B");
    }

    // ── EstabelecimentoMapper ─────────────────────────────────────────────

    private final EstabelecimentoMapper estabelecimentoMapper = new EstabelecimentoMapperImpl();

    @Test
    void estabelecimento_toModel_null() {
        assertThat(estabelecimentoMapper.toModel(null)).isNull();
    }

    @Test
    void estabelecimento_toModel() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder()
                .codigo(1).nome("Studio").cnpj("00.000.000/0001-01").fotosUrls("url1,url2").ativo(true).build();

        Estabelecimento m = estabelecimentoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getNome()).isEqualTo("Studio");
        assertThat(m.getFotosUrls()).containsExactly("url1", "url2");
    }

    @Test
    void estabelecimento_toEntity() {
        Estabelecimento m = Estabelecimento.builder().id(5).nome("Barbearia").cnpj("11.111.111/0001-11")
                .fotosUrls(List.of("img1")).ativo(true).build();

        EstabelecimentoEntity e = estabelecimentoMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(5);
        assertThat(e.getFotosUrls()).isEqualTo("img1");
    }

    @Test
    void estabelecimento_stringToList_values() {
        assertThat(estabelecimentoMapper.stringToList("a,b,c")).containsExactly("a", "b", "c");
    }

    @Test
    void estabelecimento_stringToList_null() {
        assertThat(estabelecimentoMapper.stringToList(null)).isEmpty();
    }

    @Test
    void estabelecimento_listToString_null() {
        assertThat(estabelecimentoMapper.listToString(null)).isNull();
    }

    // ── AgendaMapper ───────────────────────────────────────────────────────

    private final AgendaMapper agendaMapper = new AgendaMapperImpl();

    @Test
    void agenda_toModel_null() {
        assertThat(agendaMapper.toModel(null)).isNull();
    }

    @Test
    void agenda_toModel() {
        AgendaEntity e = AgendaEntity.builder()
                .codigo(1).codEscala(2).dataAgenda(LocalDate.now()).diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(18, 0))
                .codEstabelecimento(3).codProfissionalVinculo(4).build();

        Agenda m = agendaMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getEscalaId()).isEqualTo(2);
        assertThat(m.getEstabelecimentoId()).isEqualTo(3);
        assertThat(m.getProfissionalVinculoId()).isEqualTo(4);
    }

    @Test
    void agenda_toEntity() {
        Agenda m = Agenda.builder().id(10).escalaId(20).dataAgenda(LocalDate.now())
                .diaSemana(DiaSemanaEnum.SEXTA).horaInicio(LocalTime.of(10, 0)).horaFim(LocalTime.of(17, 0))
                .estabelecimentoId(30).profissionalVinculoId(40).build();

        AgendaEntity e = agendaMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(10);
        assertThat(e.getCodEscala()).isEqualTo(20);
        assertThat(e.getCodEstabelecimento()).isEqualTo(30);
        assertThat(e.getCodProfissionalVinculo()).isEqualTo(40);
    }

    // ── EscalaMapper ───────────────────────────────────────────────────────

    private final EscalaMapper escalaMapper = new EscalaMapperImpl();

    @Test
    void escala_toModel_null() {
        assertThat(escalaMapper.toModel(null)).isNull();
    }

    @Test
    void escala_toModel() {
        EscalaEntity e = EscalaEntity.builder()
                .codigo(1).codProfissionalVinculo(5).codEstabelecimento(6)
                .diaSemana(DiaSemanaEnum.TERCA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(12, 0)).build();

        Escala m = escalaMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getProfissionalVinculoId()).isEqualTo(5);
        assertThat(m.getEstabelecimentoId()).isEqualTo(6);
        assertThat(m.getDiaSemana()).isEqualTo(DiaSemanaEnum.TERCA);
    }

    @Test
    void escala_toEntity() {
        Escala m = Escala.builder().id(2).profissionalVinculoId(7).estabelecimentoId(8)
                .diaSemana(DiaSemanaEnum.QUARTA).horaInicio(LocalTime.of(13, 0)).horaFim(LocalTime.of(18, 0)).build();

        EscalaEntity e = escalaMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getCodProfissionalVinculo()).isEqualTo(7);
        assertThat(e.getCodEstabelecimento()).isEqualTo(8);
    }

    // ── EscalaItemMapper ──────────────────────────────────────────────────

    private final EscalaItemMapper escalaItemMapper = new EscalaItemMapperImpl();

    @Test
    void escalaItem_toModel_null() {
        assertThat(escalaItemMapper.toModel(null)).isNull();
    }

    @Test
    void escalaItem_toModel() {
        EscalaItemEntity e = EscalaItemEntity.builder().codigo(1).codEscala(3).codServico(4).ativa(true).build();

        EscalaItem m = escalaItemMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getEscalaId()).isEqualTo(3);
        assertThat(m.getServicoId()).isEqualTo(4);
    }

    @Test
    void escalaItem_toEntity() {
        EscalaItem m = EscalaItem.builder().id(2).escalaId(5).servicoId(6).ativa(false).build();

        EscalaItemEntity e = escalaItemMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getCodEscala()).isEqualTo(5);
        assertThat(e.getCodServico()).isEqualTo(6);
        assertThat(e.getAtiva()).isFalse();
    }

    // ── AgendaItemMapper ──────────────────────────────────────────────────

    private final AgendaItemMapper agendaItemMapper = new AgendaItemMapperImpl();

    @Test
    void agendaItem_toModel_null() {
        assertThat(agendaItemMapper.toModel(null)).isNull();
    }

    @Test
    void agendaItem_toModel() {
        AgendaItemEntity e = AgendaItemEntity.builder().codigo(1).codAgenda(2).codServico(3).build();

        AgendaItem m = agendaItemMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getAgendaId()).isEqualTo(2);
        assertThat(m.getServicoId()).isEqualTo(3);
    }

    @Test
    void agendaItem_toEntity() {
        AgendaItem m = AgendaItem.builder().id(4).agendaId(5).servicoId(6).build();

        AgendaItemEntity e = agendaItemMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(4);
        assertThat(e.getCodAgenda()).isEqualTo(5);
        assertThat(e.getCodServico()).isEqualTo(6);
    }

    // ── ServicoMapper ─────────────────────────────────────────────────────

    private final ServicoMapper servicoMapper = new ServicoMapperImpl();

    @Test
    void servico_toModel_null() {
        assertThat(servicoMapper.toModel(null)).isNull();
    }

    @Test
    void servico_toModel() {
        ServicoEntity e = ServicoEntity.builder()
                .codigo(1).nome("Corte").duracaoMinutos(30).preco(BigDecimal.valueOf(50)).ativo(true).build();

        Servico m = servicoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getNome()).isEqualTo("Corte");
        assertThat(m.getDuracaoMinutos()).isEqualTo(30);
        assertThat(m.getPreco()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(m.getAtivo()).isTrue();
    }

    @Test
    void servico_toEntity() {
        Servico m = Servico.builder().id(2).nome("Barba").duracaoMinutos(15).preco(BigDecimal.TEN).ativo(false).build();

        ServicoEntity e = servicoMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getNome()).isEqualTo("Barba");
        assertThat(e.getAtivo()).isFalse();
    }

    // ── ProfissionalVinculoMapper ─────────────────────────────────────────

    private final ProfissionalVinculoMapper vinculoMapper = new ProfissionalVinculoMapperImpl();

    @Test
    void vinculo_toModel_null() {
        assertThat(vinculoMapper.toModel(null)).isNull();
    }

    @Test
    void vinculo_toModel() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder()
                .codigo(1).codProfissional(10).codEstabelecimento(20).dataInicio(LocalDate.now()).build();

        ProfissionalVinculo m = vinculoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getProfissionalId()).isEqualTo(10);
        assertThat(m.getEstabelecimentoId()).isEqualTo(20);
    }

    @Test
    void vinculo_toEntity() {
        ProfissionalVinculo m = ProfissionalVinculo.builder().id(2).profissionalId(30).estabelecimentoId(40)
                .dataInicio(LocalDate.now()).build();

        ProfissionalVinculoEntity e = vinculoMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getCodProfissional()).isEqualTo(30);
        assertThat(e.getCodEstabelecimento()).isEqualTo(40);
    }

    // ── ProfissionalVinculoServicoMapper ──────────────────────────────────

    private final ProfissionalVinculoServicoMapper vinculoServicoMapper = new ProfissionalVinculoServicoMapperImpl();

    @Test
    void vinculoServico_toModel_null() {
        assertThat(vinculoServicoMapper.toModel(null)).isNull();
    }

    @Test
    void vinculoServico_toModel() {
        ProfissionalVinculoServicoEntity e = ProfissionalVinculoServicoEntity.builder()
                .codigo(1).codProfissionalVinculo(5).codServico(7).tarifa(BigDecimal.valueOf(80)).build();

        ProfissionalVinculoServico m = vinculoServicoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getProfissionalVinculoId()).isEqualTo(5);
        assertThat(m.getServicoId()).isEqualTo(7);
        assertThat(m.getTarifa()).isEqualTo(BigDecimal.valueOf(80));
    }

    @Test
    void vinculoServico_toEntity() {
        ProfissionalVinculoServico m = ProfissionalVinculoServico.builder()
                .id(2).profissionalVinculoId(6).servicoId(8).tarifa(BigDecimal.TEN).build();

        ProfissionalVinculoServicoEntity e = vinculoServicoMapper.toEntity(m);

        assertThat(e.getCodigo()).isEqualTo(2);
        assertThat(e.getCodProfissionalVinculo()).isEqualTo(6);
        assertThat(e.getCodServico()).isEqualTo(8);
    }

    // ── AvaliacaoMapper ───────────────────────────────────────────────────

    private final AvaliacaoMapper avaliacaoMapper = new AvaliacaoMapperImpl();

    @Test
    void avaliacao_toModel_null() {
        assertThat(avaliacaoMapper.toModel(null)).isNull();
    }

    @Test
    void avaliacao_toModel() {
        AvaliacaoEntity e = AvaliacaoEntity.builder()
                .codAvaliacao(1).codAgendamento(2).codCliente(3).codEstabelecimento(4)
                .codProfissionalVinculo(5).nota(5).comentario("Ótimo").dhInsert(LocalDateTime.now()).build();

        Avaliacao m = avaliacaoMapper.toModel(e);

        assertThat(m.getId()).isEqualTo(1);
        assertThat(m.getAgendamentoId()).isEqualTo(2);
        assertThat(m.getClienteId()).isEqualTo(3);
        assertThat(m.getEstabelecimentoId()).isEqualTo(4);
        assertThat(m.getProfissionalVinculoId()).isEqualTo(5);
        assertThat(m.getNota()).isEqualTo(5);
        assertThat(m.getComentario()).isEqualTo("Ótimo");
    }

    @Test
    void avaliacao_toEntity() {
        Avaliacao m = Avaliacao.builder().id(10).agendamentoId(20).clienteId(30).estabelecimentoId(40)
                .profissionalVinculoId(50).nota(3).comentario("Ok").dhInsert(LocalDateTime.now()).build();

        AvaliacaoEntity e = avaliacaoMapper.toEntity(m);

        assertThat(e.getCodAvaliacao()).isEqualTo(10);
        assertThat(e.getCodAgendamento()).isEqualTo(20);
        assertThat(e.getCodCliente()).isEqualTo(30);
        assertThat(e.getNota()).isEqualTo(3);
    }
}

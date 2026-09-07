package br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssemblerTest {

    // ── EstabelecimentoResponseAssembler ──────────────────────────────────

    @InjectMocks EstabelecimentoResponseAssembler estabelecimentoAssembler;

    @Test
    void estabelecimento_toResponse() {
        Estabelecimento e = Estabelecimento.builder()
                .id(1).nome("Studio").cnpj("00.000.000/0001-01").endereco("Rua A").telefone("11999")
                .responsavelNome("João").responsavelCpf("111.222.333-44").fotosUrls(List.of("url1")).ativo(true).build();

        EstabelecimentoResponse r = estabelecimentoAssembler.toResponse(e);

        assertThat(r.id()).isEqualTo(1);
        assertThat(r.nome()).isEqualTo("Studio");
        assertThat(r.cnpj()).isEqualTo("00.000.000/0001-01");
    }

    // ── AgendamentoResponseAssembler ──────────────────────────────────────

    @Mock ServicoRepositoryPort servicoPort;
    @Mock ClienteRepositoryPort clientePort;
    @InjectMocks AgendamentoResponseAssembler agendamentoAssembler;

    @Test
    void agendamento_toResponse_comServicoECliente() {
        Agendamento a = Agendamento.builder()
                .id(1).agendaId(10).horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 30))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false)
                .servicoId(5).clienteId(3).build();

        Servico servico = Servico.builder().id(5).nome("Corte").duracaoMinutos(30).build();
        Cliente cliente = Cliente.builder().id(3).nome("Ana").build();

        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico));
        when(clientePort.buscarPorId(3)).thenReturn(Optional.of(cliente));

        AgendamentoResponse r = agendamentoAssembler.toResponse(a);

        assertThat(r.id()).isEqualTo(1);
        assertThat(r.servico()).isNotNull();
        assertThat(r.servico().nome()).isEqualTo("Corte");
        assertThat(r.cliente()).isNotNull();
        assertThat(r.cliente().nome()).isEqualTo("Ana");
    }

    @Test
    void agendamento_toResponse_semServicoSemCliente() {
        Agendamento a = Agendamento.builder()
                .id(2).agendaId(10).horaInicio(LocalTime.of(10, 0)).horaFim(LocalTime.of(10, 30))
                .status(StatusAgendamentoEnum.DISPONIVEL).presencaConfirmada(false).build();

        AgendamentoResponse r = agendamentoAssembler.toResponse(a);

        assertThat(r.id()).isEqualTo(2);
        assertThat(r.servico()).isNull();
        assertThat(r.cliente()).isNull();
    }

    // ── AgendaResponseAssembler ───────────────────────────────────────────

    @Mock ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock ProfissionalRepositoryPort profissionalPort;
    @Mock EstabelecimentoRepositoryPort estabelecimentoPort;
    @InjectMocks AgendaResponseAssembler agendaAssembler;

    @Test
    void agenda_toResponse() {
        Agenda a = Agenda.builder().id(1).escalaId(2).dataAgenda(LocalDate.now())
                .diaSemana(DiaSemanaEnum.SEGUNDA).horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(17, 0))
                .profissionalVinculoId(10).build();

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        Profissional profissional = Profissional.builder().id(20).nome("Dr. X").ativo(true).build();
        Estabelecimento estab = Estabelecimento.builder().id(30).nome("Studio").ativo(true).build();

        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(profissional));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(estab));

        AgendaResponse r = agendaAssembler.toResponse(a);

        assertThat(r.id()).isEqualTo(1);
        assertThat(r.profissionalVinculo().profissional().nome()).isEqualTo("Dr. X");
        assertThat(r.profissionalVinculo().estabelecimento().nome()).isEqualTo("Studio");
    }

    // ── EscalaResponseAssembler ───────────────────────────────────────────

    @InjectMocks EscalaResponseAssembler escalaAssembler;

    @Test
    void escala_toResponse() {
        Escala e = Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.TERCA).horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(12, 0)).build();

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(30).build();
        Profissional profissional = Profissional.builder().id(20).nome("Dra. Y").ativo(true).build();
        Estabelecimento estab = Estabelecimento.builder().id(30).nome("Barbearia").ativo(true).build();

        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(profissional));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(estab));

        EscalaResponse r = escalaAssembler.toResponse(e);

        assertThat(r.id()).isEqualTo(1);
        assertThat(r.diaSemana()).isEqualTo(DiaSemanaEnum.TERCA);
        assertThat(r.profissionalVinculo().profissional().nome()).isEqualTo("Dra. Y");
    }

    // ── ProfissionalVinculoResponseAssembler ──────────────────────────────

    @InjectMocks ProfissionalVinculoResponseAssembler vinculoAssembler;

    @Test
    void vinculo_toVinculoResponse() {
        ProfissionalVinculo v = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20)
                .dataInicio(LocalDate.now()).build();

        Profissional p = Profissional.builder().id(10).nome("Prof. A").ativo(true).build();
        Estabelecimento est = Estabelecimento.builder().id(20).nome("Salão Z").ativo(true).build();

        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(p));
        when(estabelecimentoPort.buscarPorId(20)).thenReturn(Optional.of(est));

        VinculoResponse r = vinculoAssembler.toVinculoResponse(v);

        assertThat(r.id()).isEqualTo(1);
        assertThat(r.profissional().nome()).isEqualTo("Prof. A");
        assertThat(r.estabelecimento().nome()).isEqualTo("Salão Z");
    }

    @Test
    void vinculo_toVinculoItemResponse() {
        ProfissionalVinculoServico vi = ProfissionalVinculoServico.builder()
                .id(5).profissionalVinculoId(1).servicoId(7).build();

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20).build();
        Profissional p = Profissional.builder().id(10).nome("Prof. B").ativo(true).build();
        Estabelecimento est = Estabelecimento.builder().id(20).nome("Studio C").ativo(true).build();
        Servico s = Servico.builder().id(7).nome("Coloração").duracaoMinutos(60).build();

        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(p));
        when(estabelecimentoPort.buscarPorId(20)).thenReturn(Optional.of(est));
        when(servicoPort.buscarPorId(7)).thenReturn(Optional.of(s));

        VinculoItemResponse r = vinculoAssembler.toVinculoItemResponse(vi);

        assertThat(r.id()).isEqualTo(5);
        assertThat(r.servico()).isNotNull();
        assertThat(r.servico().nome()).isEqualTo("Coloração");
        assertThat(r.profissionalVinculo()).isNotNull();
    }
}

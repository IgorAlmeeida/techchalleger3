package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendaMapper;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ClienteMapper;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.EscalaMapper;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ProfissionalMapper;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ServicoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPortImplTest {

    // ── ServicoRepositoryPortImpl ─────────────────────────────────────────

    @Mock ServicoRepository servicoRepo;
    @Mock ServicoMapper servicoMapper;
    @InjectMocks ServicoRepositoryPortImpl servicoPortImpl;

    @Test
    void servico_buscarPorId_encontrado() {
        ServicoEntity entity = ServicoEntity.builder().codigo(1).nome("Corte").duracaoMinutos(30).ativo(true).build();
        Servico model = Servico.builder().id(1).nome("Corte").duracaoMinutos(30).ativo(true).build();
        when(servicoRepo.findById(1)).thenReturn(Optional.of(entity));
        when(servicoMapper.toModel(entity)).thenReturn(model);

        Optional<Servico> result = servicoPortImpl.buscarPorId(1);

        assertThat(result).contains(model);
    }

    @Test
    void servico_buscarPorId_vazio() {
        when(servicoRepo.findById(99)).thenReturn(Optional.empty());

        assertThat(servicoPortImpl.buscarPorId(99)).isEmpty();
    }

    @Test
    void servico_listarAtivos_pageable() {
        ServicoEntity entity = ServicoEntity.builder().codigo(1).nome("Barba").ativo(true).build();
        Servico model = Servico.builder().id(1).nome("Barba").ativo(true).build();
        Page<ServicoEntity> entityPage = new PageImpl<>(List.of(entity));
        when(servicoRepo.findAllByAtivo(eq(true), any(Pageable.class))).thenReturn(entityPage);
        when(servicoMapper.toModel(entity)).thenReturn(model);

        Page<Servico> result = servicoPortImpl.listarAtivos(PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(model);
    }

    @Test
    void servico_listarAtivos_lista() {
        ServicoEntity entity = ServicoEntity.builder().codigo(1).nome("Tintura").ativo(true).build();
        Servico model = Servico.builder().id(1).nome("Tintura").ativo(true).build();
        when(servicoRepo.findAllByAtivo(true)).thenReturn(List.of(entity));
        when(servicoMapper.toModel(entity)).thenReturn(model);

        List<Servico> result = servicoPortImpl.listarAtivos();

        assertThat(result).containsExactly(model);
    }

    @Test
    void servico_salvar() {
        Servico model = Servico.builder().id(1).nome("X").duracaoMinutos(10).preco(BigDecimal.TEN).ativo(true).build();
        ServicoEntity entity = ServicoEntity.builder().codigo(1).nome("X").ativo(true).build();
        when(servicoMapper.toEntity(model)).thenReturn(entity);
        when(servicoRepo.save(entity)).thenReturn(entity);
        when(servicoMapper.toModel(entity)).thenReturn(model);

        Servico saved = servicoPortImpl.salvar(model);

        assertThat(saved).isEqualTo(model);
    }

    // ── ClienteRepositoryPortImpl ─────────────────────────────────────────

    @Mock ClienteRepository clienteRepo;
    @Mock ClienteMapper clienteMapper;
    @InjectMocks ClienteRepositoryPortImpl clientePortImpl;

    @Test
    void cliente_buscarPorId() {
        ClienteEntity e = ClienteEntity.builder().codigo(1).codUsuario(10).nome("Ana").build();
        Cliente m = Cliente.builder().id(1).usuarioId(10).nome("Ana").build();
        when(clienteRepo.findById(1)).thenReturn(Optional.of(e));
        when(clienteMapper.toModel(e)).thenReturn(m);

        assertThat(clientePortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void cliente_buscarPorUsuarioId() {
        ClienteEntity e = ClienteEntity.builder().codigo(1).codUsuario(5).build();
        Cliente m = Cliente.builder().id(1).usuarioId(5).build();
        when(clienteRepo.findByCodUsuario(5)).thenReturn(Optional.of(e));
        when(clienteMapper.toModel(e)).thenReturn(m);

        assertThat(clientePortImpl.buscarPorUsuarioId(5)).contains(m);
    }

    @Test
    void cliente_buscarPorCpf() {
        ClienteEntity e = ClienteEntity.builder().codigo(1).cpf("123").build();
        Cliente m = Cliente.builder().id(1).cpf("123").build();
        when(clienteRepo.findByCpf("123")).thenReturn(Optional.of(e));
        when(clienteMapper.toModel(e)).thenReturn(m);

        assertThat(clientePortImpl.buscarPorCpf("123")).contains(m);
    }

    @Test
    void cliente_buscarPorEmail() {
        ClienteEntity e = ClienteEntity.builder().codigo(1).email("x@x.com").build();
        Cliente m = Cliente.builder().id(1).email("x@x.com").build();
        when(clienteRepo.findByEmail("x@x.com")).thenReturn(Optional.of(e));
        when(clienteMapper.toModel(e)).thenReturn(m);

        assertThat(clientePortImpl.buscarPorEmail("x@x.com")).contains(m);
    }

    @Test
    void cliente_existePorCpf() {
        when(clienteRepo.existsByCpf("456")).thenReturn(true);

        assertThat(clientePortImpl.existePorCpf("456")).isTrue();
    }

    @Test
    void cliente_salvar() {
        Cliente m = Cliente.builder().id(1).nome("Bob").cpf("789").build();
        ClienteEntity e = ClienteEntity.builder().codigo(1).nome("Bob").cpf("789").build();
        when(clienteMapper.toEntity(m)).thenReturn(e);
        when(clienteRepo.save(e)).thenReturn(e);
        when(clienteMapper.toModel(e)).thenReturn(m);

        assertThat(clientePortImpl.salvar(m)).isEqualTo(m);
    }

    // ── ProfissionalRepositoryPortImpl ────────────────────────────────────

    @Mock ProfissionalRepository profissionalRepo;
    @Mock ProfissionalMapper profissionalMapper;
    @InjectMocks ProfissionalRepositoryPortImpl profissionalPortImpl;

    @Test
    void profissional_buscarPorId() {
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).nome("Dr").build();
        Profissional m = Profissional.builder().id(1).nome("Dr").ativo(true).build();
        when(profissionalRepo.findById(1)).thenReturn(Optional.of(e));
        when(profissionalMapper.toModel(e)).thenReturn(m);

        assertThat(profissionalPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void profissional_buscarPorUsuarioId() {
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).codUsuario(7).build();
        Profissional m = Profissional.builder().id(1).usuarioId(7).ativo(true).build();
        when(profissionalRepo.findByCodUsuario(7)).thenReturn(Optional.of(e));
        when(profissionalMapper.toModel(e)).thenReturn(m);

        assertThat(profissionalPortImpl.buscarPorUsuarioId(7)).contains(m);
    }

    @Test
    void profissional_buscarPorEmail() {
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).email("p@x.com").build();
        Profissional m = Profissional.builder().id(1).email("p@x.com").ativo(true).build();
        when(profissionalRepo.findByEmail("p@x.com")).thenReturn(Optional.of(e));
        when(profissionalMapper.toModel(e)).thenReturn(m);

        assertThat(profissionalPortImpl.buscarPorEmail("p@x.com")).contains(m);
    }

    @Test
    void profissional_listarTodos() {
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).build();
        Profissional m = Profissional.builder().id(1).ativo(true).build();
        when(profissionalRepo.findAll()).thenReturn(List.of(e));
        when(profissionalMapper.toModel(e)).thenReturn(m);

        assertThat(profissionalPortImpl.listarTodos()).containsExactly(m);
    }

    @Test
    void profissional_listarComFiltros() {
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).nome("Ana").build();
        Profissional m = Profissional.builder().id(1).nome("Ana").ativo(true).build();
        Page<ProfissionalEntity> page = new PageImpl<>(List.of(e));
        when(profissionalRepo.buscarComFiltros(any(), any(), anyBoolean(), any())).thenReturn(page);
        when(profissionalMapper.toModel(e)).thenReturn(m);

        Page<Profissional> result = profissionalPortImpl.listarComFiltros(null, null, false, PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(m);
    }

    @Test
    void profissional_salvar() {
        Profissional m = Profissional.builder().id(1).nome("Dr").ativo(true).build();
        ProfissionalEntity e = ProfissionalEntity.builder().codigo(1).nome("Dr").build();
        when(profissionalMapper.toEntity(m)).thenReturn(e);
        when(profissionalRepo.save(e)).thenReturn(e);
        when(profissionalMapper.toModel(e)).thenReturn(m);

        assertThat(profissionalPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── EscalaRepositoryPortImpl ──────────────────────────────────────────

    @Mock EscalaRepository escalaRepo;
    @Mock EscalaMapper escalaMapper;
    @InjectMocks EscalaRepositoryPortImpl escalaPortImpl;

    @Test
    void escala_buscarPorId() {
        EscalaEntity e = EscalaEntity.builder().codigo(1).codProfissionalVinculo(5).build();
        Escala m = Escala.builder().id(1).profissionalVinculoId(5).build();
        when(escalaRepo.findById(1)).thenReturn(Optional.of(e));
        when(escalaMapper.toModel(e)).thenReturn(m);

        assertThat(escalaPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void escala_listarPorProfissionalVinculoId() {
        EscalaEntity e = EscalaEntity.builder().codigo(1).codProfissionalVinculo(10).build();
        Escala m = Escala.builder().id(1).profissionalVinculoId(10).build();
        when(escalaRepo.findByCodProfissionalVinculo(10)).thenReturn(List.of(e));
        when(escalaMapper.toModel(e)).thenReturn(m);

        assertThat(escalaPortImpl.listarPorProfissionalVinculoId(10)).containsExactly(m);
    }

    @Test
    void escala_listarPorFiltros() {
        EscalaEntity e = EscalaEntity.builder().codigo(1).build();
        Escala m = Escala.builder().id(1).build();
        when(escalaRepo.listarPorFiltros(any(), any())).thenReturn(List.of(e));
        when(escalaMapper.toModel(e)).thenReturn(m);

        assertThat(escalaPortImpl.listarPorFiltros(1, 2)).containsExactly(m);
    }

    @Test
    void escala_salvar() {
        Escala m = Escala.builder().id(1).profissionalVinculoId(5).build();
        EscalaEntity e = EscalaEntity.builder().codigo(1).codProfissionalVinculo(5).build();
        when(escalaMapper.toEntity(m)).thenReturn(e);
        when(escalaRepo.save(e)).thenReturn(e);
        when(escalaMapper.toModel(e)).thenReturn(m);

        assertThat(escalaPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── AgendaRepositoryPortImpl ──────────────────────────────────────────

    @Mock AgendaRepository agendaRepo;
    @Mock AgendaMapper agendaMapper;
    @InjectMocks AgendaRepositoryPortImpl agendaPortImpl;

    @Test
    void agenda_buscarPorId() {
        AgendaEntity e = AgendaEntity.builder().codigo(1).codProfissionalVinculo(5).build();
        Agenda m = Agenda.builder().id(1).profissionalVinculoId(5).build();
        when(agendaRepo.findById(1)).thenReturn(Optional.of(e));
        when(agendaMapper.toModel(e)).thenReturn(m);

        assertThat(agendaPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void agenda_listarPorProfissionalVinculoId() {
        AgendaEntity e = AgendaEntity.builder().codigo(1).codProfissionalVinculo(7).build();
        Agenda m = Agenda.builder().id(1).profissionalVinculoId(7).build();
        when(agendaRepo.findByCodProfissionalVinculo(7)).thenReturn(List.of(e));
        when(agendaMapper.toModel(e)).thenReturn(m);

        assertThat(agendaPortImpl.listarPorProfissionalVinculoId(7)).containsExactly(m);
    }

    @Test
    void agenda_existeAgendaFuturaPorVinculo() {
        when(agendaRepo.existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(1), any(LocalDate.class))).thenReturn(true);

        assertThat(agendaPortImpl.existeAgendaFuturaPorVinculo(1)).isTrue();
    }

    @Test
    void agenda_existeAgendaPorVinculoEData() {
        LocalDate data = LocalDate.of(2026, 9, 10);
        when(agendaRepo.existsByCodProfissionalVinculoAndDataAgenda(1, data)).thenReturn(false);

        assertThat(agendaPortImpl.existeAgendaPorVinculoEData(1, data)).isFalse();
    }

    @Test
    void agenda_listarFuturasPorVinculo() {
        AgendaEntity e = AgendaEntity.builder().codigo(1).dataAgenda(LocalDate.now().plusDays(1)).build();
        Agenda m = Agenda.builder().id(1).dataAgenda(LocalDate.now().plusDays(1)).build();
        when(agendaRepo.findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(3), any())).thenReturn(List.of(e));
        when(agendaMapper.toModel(e)).thenReturn(m);

        assertThat(agendaPortImpl.listarFuturasPorVinculo(3)).containsExactly(m);
    }

    @Test
    void agenda_salvar() {
        Agenda m = Agenda.builder().id(1).dataAgenda(LocalDate.now()).build();
        AgendaEntity e = AgendaEntity.builder().codigo(1).dataAgenda(LocalDate.now()).build();
        when(agendaMapper.toEntity(m)).thenReturn(e);
        when(agendaRepo.save(e)).thenReturn(e);
        when(agendaMapper.toModel(e)).thenReturn(m);

        assertThat(agendaPortImpl.salvar(m)).isEqualTo(m);
    }

}

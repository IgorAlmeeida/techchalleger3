package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoreRepositoryPortImplTest {

    // ── EstabelecimentoRepositoryPortImpl ────────────────────────────────────

    @Mock EstabelecimentoRepository estabelecimentoRepo;
    @Mock EstabelecimentoMapper estabelecimentoMapper;
    @InjectMocks EstabelecimentoRepositoryPortImpl estabelecimentoPortImpl;

    @Test
    void estabelecimento_buscarPorId() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(1).nome("Clínica").cnpj("12345678000195").build();
        Estabelecimento m = Estabelecimento.builder().id(1).nome("Clínica").cnpj("12345678000195").build();
        when(estabelecimentoRepo.findById(1)).thenReturn(Optional.of(e));
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        assertThat(estabelecimentoPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void estabelecimento_buscarPorId_vazio() {
        when(estabelecimentoRepo.findById(99)).thenReturn(Optional.empty());
        assertThat(estabelecimentoPortImpl.buscarPorId(99)).isEmpty();
    }

    @Test
    void estabelecimento_existePorCnpj() {
        when(estabelecimentoRepo.existsByCnpj("123")).thenReturn(true);
        assertThat(estabelecimentoPortImpl.existePorCnpj("123")).isTrue();
    }

    @Test
    void estabelecimento_existePorCnpjExcluindoId() {
        when(estabelecimentoRepo.existsByCnpjAndCodigoNot("123", 5)).thenReturn(false);
        assertThat(estabelecimentoPortImpl.existePorCnpjExcluindoId("123", 5)).isFalse();
    }

    @Test
    void estabelecimento_listarAtivos_pageable() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(1).nome("A").cnpj("1").build();
        Estabelecimento m = Estabelecimento.builder().id(1).nome("A").build();
        Page<EstabelecimentoEntity> page = new PageImpl<>(List.of(e));
        when(estabelecimentoRepo.findAllByAtivo(eq(true), any(Pageable.class))).thenReturn(page);
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        assertThat(estabelecimentoPortImpl.listarAtivos(PageRequest.of(0, 10)).getContent()).containsExactly(m);
    }

    @Test
    void estabelecimento_listarAtivos_lista() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(2).nome("B").cnpj("2").build();
        Estabelecimento m = Estabelecimento.builder().id(2).nome("B").build();
        when(estabelecimentoRepo.findAllByAtivo(true)).thenReturn(List.of(e));
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        assertThat(estabelecimentoPortImpl.listarAtivos()).containsExactly(m);
    }

    @Test
    void estabelecimento_listarPorIds() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(1).nome("X").cnpj("3").build();
        Estabelecimento m = Estabelecimento.builder().id(1).nome("X").build();
        when(estabelecimentoRepo.findAllByCodigoIn(List.of(1, 2))).thenReturn(List.of(e));
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        assertThat(estabelecimentoPortImpl.listarPorIds(List.of(1, 2))).containsExactly(m);
    }

    @Test
    void estabelecimento_buscarComFiltros() {
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(1).nome("Y").cnpj("4").build();
        Estabelecimento m = Estabelecimento.builder().id(1).nome("Y").build();
        Page<EstabelecimentoEntity> page = new PageImpl<>(List.of(e));
        when(estabelecimentoRepo.buscarComFiltros(any(), any(), any())).thenReturn(page);
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        Page<Estabelecimento> result = estabelecimentoPortImpl.buscarComFiltros(
                null, null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).containsExactly(m);
    }

    @Test
    void estabelecimento_salvar() {
        Estabelecimento m = Estabelecimento.builder().id(1).nome("Z").cnpj("5").build();
        EstabelecimentoEntity e = EstabelecimentoEntity.builder().codigo(1).nome("Z").cnpj("5").build();
        when(estabelecimentoMapper.toEntity(m)).thenReturn(e);
        when(estabelecimentoRepo.save(e)).thenReturn(e);
        when(estabelecimentoMapper.toModel(e)).thenReturn(m);
        assertThat(estabelecimentoPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── ProfissionalVinculoRepositoryPortImpl ─────────────────────────────────

    @Mock ProfissionalVinculoRepository profissionalVinculoRepo;
    @Mock ProfissionalVinculoMapper profissionalVinculoMapper;
    @InjectMocks ProfissionalVinculoRepositoryPortImpl profissionalVinculoPortImpl;

    @Test
    void vinculo_buscarPorId() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(1).codProfissional(10).codEstabelecimento(20).build();
        ProfissionalVinculo m = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20).build();
        when(profissionalVinculoRepo.findById(1)).thenReturn(Optional.of(e));
        when(profissionalVinculoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void vinculo_listarPorProfissionalId() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(1).codProfissional(5).codEstabelecimento(10).build();
        ProfissionalVinculo m = ProfissionalVinculo.builder().id(1).profissionalId(5).estabelecimentoId(10).build();
        when(profissionalVinculoRepo.findByCodProfissional(5)).thenReturn(List.of(e));
        when(profissionalVinculoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoPortImpl.listarPorProfissionalId(5)).containsExactly(m);
    }

    @Test
    void vinculo_listarEstabelecimentoIdsAtivosPorProfissional() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(1).codProfissional(5).codEstabelecimento(10).build();
        when(profissionalVinculoRepo.findByCodProfissionalAndDataFimIsNull(5)).thenReturn(List.of(e));
        assertThat(profissionalVinculoPortImpl.listarEstabelecimentoIdsAtivosPorProfissional(5)).containsExactly(10);
    }

    @Test
    void vinculo_listarIdsPorEstabelecimento() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(7).codProfissional(5).codEstabelecimento(10).build();
        when(profissionalVinculoRepo.findByCodEstabelecimento(10)).thenReturn(List.of(e));
        assertThat(profissionalVinculoPortImpl.listarIdsPorEstabelecimento(10)).containsExactly(7);
    }

    @Test
    void vinculo_listarPorFiltros() {
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(1).codProfissional(1).codEstabelecimento(2).build();
        ProfissionalVinculo m = ProfissionalVinculo.builder().id(1).profissionalId(1).estabelecimentoId(2).build();
        Page<ProfissionalVinculoEntity> page = new PageImpl<>(List.of(e));
        when(profissionalVinculoRepo.listarPorFiltros(any(), any(), any())).thenReturn(page);
        when(profissionalVinculoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoPortImpl.listarPorFiltros(1, 2, PageRequest.of(0, 10)).getContent()).containsExactly(m);
    }

    @Test
    void vinculo_existeVinculoAtivo() {
        when(profissionalVinculoRepo.existsByCodProfissionalAndCodEstabelecimentoAndDataFimIsNull(1, 2)).thenReturn(true);
        assertThat(profissionalVinculoPortImpl.existeVinculoAtivo(1, 2)).isTrue();
    }

    @Test
    void vinculo_existeVinculoAtivoPorEstabelecimento() {
        when(profissionalVinculoRepo.existsByCodEstabelecimentoAndDataFimIsNull(5)).thenReturn(false);
        assertThat(profissionalVinculoPortImpl.existeVinculoAtivoPorEstabelecimento(5)).isFalse();
    }

    @Test
    void vinculo_salvar() {
        ProfissionalVinculo m = ProfissionalVinculo.builder().id(1).profissionalId(10).estabelecimentoId(20).build();
        ProfissionalVinculoEntity e = ProfissionalVinculoEntity.builder().codigo(1).codProfissional(10).codEstabelecimento(20).build();
        when(profissionalVinculoMapper.toEntity(m)).thenReturn(e);
        when(profissionalVinculoRepo.save(e)).thenReturn(e);
        when(profissionalVinculoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── AvaliacaoRepositoryPortImpl ─────────────────────────────────────────

    @Mock AvaliacaoRepository avaliacaoRepo;
    @Mock AvaliacaoMapper avaliacaoMapper;
    @InjectMocks AvaliacaoRepositoryPortImpl avaliacaoPortImpl;

    @Test
    void avaliacao_salvar() {
        Avaliacao m = Avaliacao.builder().id(1).nota(5).build();
        AvaliacaoEntity e = AvaliacaoEntity.builder().codAvaliacao(1).nota(5).build();
        when(avaliacaoMapper.toEntity(m)).thenReturn(e);
        when(avaliacaoRepo.save(e)).thenReturn(e);
        when(avaliacaoMapper.toModel(e)).thenReturn(m);
        assertThat(avaliacaoPortImpl.salvar(m)).isEqualTo(m);
    }

    @Test
    void avaliacao_buscarPorId() {
        AvaliacaoEntity e = AvaliacaoEntity.builder().codAvaliacao(1).nota(4).build();
        Avaliacao m = Avaliacao.builder().id(1).nota(4).build();
        when(avaliacaoRepo.findById(1)).thenReturn(Optional.of(e));
        when(avaliacaoMapper.toModel(e)).thenReturn(m);
        assertThat(avaliacaoPortImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void avaliacao_listarPorEstabelecimentoId() {
        AvaliacaoEntity e = AvaliacaoEntity.builder().codAvaliacao(1).codEstabelecimento(10).nota(3).build();
        Avaliacao m = Avaliacao.builder().id(1).estabelecimentoId(10).nota(3).build();
        when(avaliacaoRepo.findByCodEstabelecimento(10)).thenReturn(List.of(e));
        when(avaliacaoMapper.toModel(e)).thenReturn(m);
        assertThat(avaliacaoPortImpl.listarPorEstabelecimentoId(10)).containsExactly(m);
    }

    @Test
    void avaliacao_listarPorProfissionalVinculoId() {
        AvaliacaoEntity e = AvaliacaoEntity.builder().codAvaliacao(1).codProfissionalVinculo(7).nota(5).build();
        Avaliacao m = Avaliacao.builder().id(1).profissionalVinculoId(7).nota(5).build();
        when(avaliacaoRepo.findByCodProfissionalVinculo(7)).thenReturn(List.of(e));
        when(avaliacaoMapper.toModel(e)).thenReturn(m);
        assertThat(avaliacaoPortImpl.listarPorProfissionalVinculoId(7)).containsExactly(m);
    }

    @Test
    void avaliacao_calcularNotaMedia() {
        when(avaliacaoRepo.calcularNotaMedia(10)).thenReturn(4.5);
        assertThat(avaliacaoPortImpl.calcularNotaMedia(10)).isEqualTo(4.5);
    }

    @Test
    void avaliacao_existePorAgendamentoId() {
        when(avaliacaoRepo.existsByCodAgendamento(99)).thenReturn(true);
        assertThat(avaliacaoPortImpl.existePorAgendamentoId(99)).isTrue();
    }

    // ── EscalaItemRepositoryPortImpl ──────────────────────────────────────────

    @Mock EscalaItemRepository escalaItemRepo;
    @Mock EscalaItemMapper escalaItemMapper;
    @InjectMocks EscalaItemRepositoryPortImpl escalaItemPortImpl;

    @Test
    void escalaItem_listarPorEscalaId() {
        EscalaItemEntity e = EscalaItemEntity.builder().codigo(1).codEscala(5).codServico(10).ativa(true).build();
        EscalaItem m = EscalaItem.builder().id(1).escalaId(5).servicoId(10).ativa(true).build();
        when(escalaItemRepo.findByCodEscala(5)).thenReturn(List.of(e));
        when(escalaItemMapper.toModel(e)).thenReturn(m);
        assertThat(escalaItemPortImpl.listarPorEscalaId(5)).containsExactly(m);
    }

    @Test
    void escalaItem_listarAtivosPorEscalaId() {
        EscalaItemEntity e = EscalaItemEntity.builder().codigo(1).codEscala(5).codServico(10).ativa(true).build();
        EscalaItem m = EscalaItem.builder().id(1).escalaId(5).servicoId(10).ativa(true).build();
        when(escalaItemRepo.findByCodEscalaAndAtivaTrue(5)).thenReturn(List.of(e));
        when(escalaItemMapper.toModel(e)).thenReturn(m);
        assertThat(escalaItemPortImpl.listarAtivosPorEscalaId(5)).containsExactly(m);
    }

    @Test
    void escalaItem_deletarPorEscalaId() {
        escalaItemPortImpl.deletarPorEscalaId(5);
        verify(escalaItemRepo).deleteByCodEscala(5);
    }

    @Test
    void escalaItem_salvar() {
        EscalaItem m = EscalaItem.builder().id(1).escalaId(5).servicoId(10).ativa(true).build();
        EscalaItemEntity e = EscalaItemEntity.builder().codigo(1).codEscala(5).codServico(10).ativa(true).build();
        when(escalaItemMapper.toEntity(m)).thenReturn(e);
        when(escalaItemRepo.save(e)).thenReturn(e);
        when(escalaItemMapper.toModel(e)).thenReturn(m);
        assertThat(escalaItemPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── AgendaItemRepositoryPortImpl ──────────────────────────────────────────

    @Mock AgendaItemRepository agendaItemRepo;
    @Mock AgendaItemMapper agendaItemMapper;
    @InjectMocks AgendaItemRepositoryPortImpl agendaItemPortImpl;

    @Test
    void agendaItem_listarPorAgendaId() {
        AgendaItemEntity e = AgendaItemEntity.builder().codigo(1).codAgenda(10).codServico(5).build();
        AgendaItem m = AgendaItem.builder().id(1).agendaId(10).servicoId(5).build();
        when(agendaItemRepo.findByCodAgenda(10)).thenReturn(List.of(e));
        when(agendaItemMapper.toModel(e)).thenReturn(m);
        assertThat(agendaItemPortImpl.listarPorAgendaId(10)).containsExactly(m);
    }

    @Test
    void agendaItem_existePorAgendaIdsEServico_listaVazia_retornaFalse() {
        assertThat(agendaItemPortImpl.existePorAgendaIdsEServico(List.of(), 5)).isFalse();
        assertThat(agendaItemPortImpl.existePorAgendaIdsEServico(null, 5)).isFalse();
    }

    @Test
    void agendaItem_existePorAgendaIdsEServico_comIds() {
        when(agendaItemRepo.existsByCodAgendaInAndCodServico(List.of(1, 2), 5)).thenReturn(true);
        assertThat(agendaItemPortImpl.existePorAgendaIdsEServico(List.of(1, 2), 5)).isTrue();
    }

    @Test
    void agendaItem_salvar() {
        AgendaItem m = AgendaItem.builder().id(1).agendaId(10).servicoId(5).build();
        AgendaItemEntity e = AgendaItemEntity.builder().codigo(1).codAgenda(10).codServico(5).build();
        when(agendaItemMapper.toEntity(m)).thenReturn(e);
        when(agendaItemRepo.save(e)).thenReturn(e);
        when(agendaItemMapper.toModel(e)).thenReturn(m);
        assertThat(agendaItemPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── ProfissionalVinculoServicoRepositoryPortImpl ─────────────────────────

    @Mock ProfissionalVinculoServicoRepository profissionalVinculoServicoRepo;
    @Mock ProfissionalVinculoServicoMapper profissionalVinculoServicoMapper;
    @InjectMocks ProfissionalVinculoServicoRepositoryPortImpl profissionalVinculoServicoPortImpl;

    @Test
    void vinculoServico_listarPorProfissionalVinculoId() {
        ProfissionalVinculoServicoEntity e = ProfissionalVinculoServicoEntity.builder()
                .codigo(1).codProfissionalVinculo(3).codServico(7).build();
        ProfissionalVinculoServico m = ProfissionalVinculoServico.builder().id(1).profissionalVinculoId(3).servicoId(7).build();
        when(profissionalVinculoServicoRepo.findByCodProfissionalVinculo(3)).thenReturn(List.of(e));
        when(profissionalVinculoServicoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoServicoPortImpl.listarPorProfissionalVinculoId(3)).containsExactly(m);
    }

    @Test
    void vinculoServico_existePorVinculoEServico() {
        when(profissionalVinculoServicoRepo.existsByCodProfissionalVinculoAndCodServico(3, 7)).thenReturn(true);
        assertThat(profissionalVinculoServicoPortImpl.existePorVinculoEServico(3, 7)).isTrue();
    }

    @Test
    void vinculoServico_deletarPorVinculoEServico() {
        profissionalVinculoServicoPortImpl.deletarPorVinculoEServico(3, 7);
        verify(profissionalVinculoServicoRepo).deleteByCodProfissionalVinculoAndCodServico(3, 7);
    }

    @Test
    void vinculoServico_salvar() {
        ProfissionalVinculoServico m = ProfissionalVinculoServico.builder().id(1).profissionalVinculoId(3).servicoId(7).build();
        ProfissionalVinculoServicoEntity e = ProfissionalVinculoServicoEntity.builder()
                .codigo(1).codProfissionalVinculo(3).codServico(7).build();
        when(profissionalVinculoServicoMapper.toEntity(m)).thenReturn(e);
        when(profissionalVinculoServicoRepo.save(e)).thenReturn(e);
        when(profissionalVinculoServicoMapper.toModel(e)).thenReturn(m);
        assertThat(profissionalVinculoServicoPortImpl.salvar(m)).isEqualTo(m);
    }

    // ── UsuarioRepositoryPortImpl ─────────────────────────────────────────────

    @Mock UsuarioRepository usuarioRepo;
    @Mock UsuarioMapper usuarioMapper;
    @InjectMocks UsuarioRepositoryPortImpl usuarioPortImpl;

    @Test
    void usuario_buscarPorCodKeycloak() {
        UsuarioEntity e = UsuarioEntity.builder().codigo(1).codKeycloak("sub-abc").build();
        Usuario m = Usuario.builder().id(1).keycloakId("sub-abc").build();
        when(usuarioRepo.findByCodKeycloak("sub-abc")).thenReturn(Optional.of(e));
        when(usuarioMapper.toModel(e)).thenReturn(m);
        assertThat(usuarioPortImpl.buscarPorCodKeycloak("sub-abc")).contains(m);
    }

    @Test
    void usuario_buscarPorId() {
        UsuarioEntity e = UsuarioEntity.builder().codigo(5).codKeycloak("sub-x").build();
        Usuario m = Usuario.builder().id(5).keycloakId("sub-x").build();
        when(usuarioRepo.findById(5)).thenReturn(Optional.of(e));
        when(usuarioMapper.toModel(e)).thenReturn(m);
        assertThat(usuarioPortImpl.buscarPorId(5)).contains(m);
    }

    @Test
    void usuario_salvar() {
        Usuario m = Usuario.builder().id(1).keycloakId("sub-new").build();
        UsuarioEntity e = UsuarioEntity.builder().codigo(1).codKeycloak("sub-new").build();
        when(usuarioMapper.toEntity(m)).thenReturn(e);
        when(usuarioRepo.save(e)).thenReturn(e);
        when(usuarioMapper.toModel(e)).thenReturn(m);
        assertThat(usuarioPortImpl.salvar(m)).isEqualTo(m);
    }
}

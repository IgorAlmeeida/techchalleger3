package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.*;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.ProfissionalVinculoResponseAssembler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfissionalVinculoController.class)
@Import(SecurityConfig.class)
class ProfissionalVinculoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CriarVinculoUseCase criarVinculoUseCase;
    @MockitoBean AssociarServicoAoVinculoUseCase associarServicoUseCase;
    @MockitoBean ListarVinculosUseCase listarVinculosUseCase;
    @MockitoBean ListarServicosDoVinculoUseCase listarServicosDoVinculoUseCase;
    @MockitoBean ListarOfertaDoEstabelecimentoUseCase listarOfertaDoEstabelecimentoUseCase;
    @MockitoBean DesvincularItemUseCase desvincularItemUseCase;
    @MockitoBean DesvincularProfissionalUseCase desvincularProfissionalUseCase;
    @MockitoBean ProfissionalVinculoResponseAssembler assembler;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean SincronizarUsuarioFilter sincronizarFilter;
    @MockitoBean ContextoEstabelecimentoFilter contextoFilter;

    @BeforeEach
    void filtros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private ProfissionalVinculo vinculo() {
        return ProfissionalVinculo.builder().id(1).profissionalId(5).estabelecimentoId(10).build();
    }

    @Test
    void criar_semAuth_retorna401() throws Exception {
        mockMvc.perform(post("/api/profissional-vinculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalId":5,"estabelecimentoId":10,"dataInicio":"2026-10-01"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_comRoleAdmin_retorna201() throws Exception {
        when(criarVinculoUseCase.executar(any(), any(), any())).thenReturn(vinculo());
        when(assembler.toVinculoResponse(any())).thenReturn(null);

        mockMvc.perform(post("/api/profissional-vinculos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalId":5,"estabelecimentoId":10,"dataInicio":"2026-10-01"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void criar_comRoleProfissional_retorna403() throws Exception {
        mockMvc.perform(post("/api/profissional-vinculos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalId":5,"estabelecimentoId":10,"dataInicio":"2026-10-01"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void associarServico_comRoleAdmin_retorna201() throws Exception {
        ProfissionalVinculoServico item = ProfissionalVinculoServico.builder().id(1).profissionalVinculoId(1).servicoId(7).build();
        when(associarServicoUseCase.executar(anyInt(), anyInt())).thenReturn(item);
        when(assembler.toVinculoItemResponse(any())).thenReturn(null);

        mockMvc.perform(post("/api/profissional-vinculos/1/itens")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"servicoId":7}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void associarServico_semAuth_retorna401() throws Exception {
        mockMvc.perform(post("/api/profissional-vinculos/1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"servicoId":7}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_comRoleAdmin_retorna200() throws Exception {
        when(listarVinculosUseCase.executar(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(vinculo())));
        when(assembler.toVinculoResponse(any())).thenReturn(null);

        mockMvc.perform(get("/api/profissional-vinculos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void listar_comRoleProfissional_retorna403() throws Exception {
        mockMvc.perform(get("/api/profissional-vinculos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarItens_comRoleAdmin_retorna200() throws Exception {
        when(listarServicosDoVinculoUseCase.executar(anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/profissional-vinculos/1/itens")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void listarOferta_comRoleCliente_retorna200() throws Exception {
        when(listarOfertaDoEstabelecimentoUseCase.executar(anyInt(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/profissional-vinculos/oferta")
                        .param("estabelecimentoId", "10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk());
    }

    @Test
    void listarOferta_comRoleAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/profissional-vinculos/oferta")
                        .param("estabelecimentoId", "10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void desvincularServico_comRoleAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/profissional-vinculos/1/itens/5")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void desvincularServico_semAuth_retorna401() throws Exception {
        mockMvc.perform(delete("/api/profissional-vinculos/1/itens/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void desvincularProfissional_comRoleAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/profissional-vinculos/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void desvincularProfissional_comRoleProfissional_retorna403() throws Exception {
        mockMvc.perform(delete("/api/profissional-vinculos/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isForbidden());
    }
}

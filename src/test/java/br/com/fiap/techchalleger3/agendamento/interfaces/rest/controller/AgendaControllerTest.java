package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.GerarAgendaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarAgendasUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.AgendaResponseAssembler;

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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendaController.class)
@Import(SecurityConfig.class)
class AgendaControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean GerarAgendaUseCase gerarAgendaUseCase;
    @MockitoBean CancelarAgendaUseCase cancelarAgendaUseCase;
    @MockitoBean ListarAgendasUseCase listarAgendasUseCase;
    @MockitoBean AgendaResponseAssembler assembler;
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

    @Test
    void gerar_semAuth_retorna401() throws Exception {
        mockMvc.perform(post("/api/agendas/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"escalaId":1,"dataInicio":"2026-10-01","dataFim":"2026-10-31"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void gerar_comRoleProfissional_retorna201() throws Exception {
        Agenda agenda = Agenda.builder().id(1).profissionalVinculoId(10)
                .dataAgenda(LocalDate.of(2026, 10, 6)).build();
        when(gerarAgendaUseCase.executar(any(), any(), any(), anyString(), anyBoolean()))
                .thenReturn(List.of(agenda));
        when(assembler.toResponse(any())).thenReturn(null);

        mockMvc.perform(post("/api/agendas/gerar")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"escalaId":1,"dataInicio":"2026-10-01","dataFim":"2026-10-31"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void gerar_comRoleAdmin_retorna201() throws Exception {
        when(gerarAgendaUseCase.executar(any(), any(), any(), anyString(), anyBoolean()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/agendas/gerar")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"escalaId":1,"dataInicio":"2026-10-01","dataFim":"2026-10-31"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void gerar_comRoleCliente_retorna403() throws Exception {
        mockMvc.perform(post("/api/agendas/gerar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"escalaId":1,"dataInicio":"2026-10-01","dataFim":"2026-10-31"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelar_semAuth_retorna401() throws Exception {
        mockMvc.perform(patch("/api/agendas/1/cancelar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelar_comRoleProfissional_retorna204() throws Exception {
        mockMvc.perform(patch("/api/agendas/1/cancelar")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelar_comRoleAdmin_retorna204() throws Exception {
        mockMvc.perform(patch("/api/agendas/1/cancelar")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void listar_semAuth_retorna401() throws Exception {
        mockMvc.perform(get("/api/agendas").param("profissionalVinculoId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_comRoleProfissional_retorna200() throws Exception {
        when(listarAgendasUseCase.executar(any(), any(), any(), any(), anyString(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/agendas")
                        .param("profissionalVinculoId", "10")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk());
    }

    @Test
    void listar_comRoleCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/agendas")
                        .param("profissionalVinculoId", "10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }
}

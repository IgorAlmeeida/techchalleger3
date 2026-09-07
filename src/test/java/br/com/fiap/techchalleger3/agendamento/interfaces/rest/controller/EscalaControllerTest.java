package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.AtualizarEscalaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarEscalaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarEscalasUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EscalaResponseAssembler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EscalaController.class)
@Import(SecurityConfig.class)
class EscalaControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean CriarEscalaUseCase criarUseCase;
    @MockBean AtualizarEscalaUseCase atualizarUseCase;
    @MockBean ListarEscalasUseCase listarUseCase;
    @MockBean EscalaResponseAssembler assembler;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SincronizarUsuarioFilter sincronizarFilter;
    @MockBean ContextoEstabelecimentoFilter contextoFilter;

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

    private Escala escala() {
        return Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(17, 0)).build();
    }

    @Test
    void criar_semAuth_retorna401() throws Exception {
        mockMvc.perform(post("/api/escalas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalVinculoId":1,"diaSemana":"SEGUNDA",
                                 "horaInicio":"09:00:00","horaFim":"17:00:00","servicosIds":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_comRoleProfissional_retorna201() throws Exception {
        when(criarUseCase.executar(any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(escala());
        when(assembler.toResponse(any())).thenReturn(null);

        mockMvc.perform(post("/api/escalas")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalVinculoId":1,"diaSemana":"SEGUNDA",
                                 "horaInicio":"09:00:00","horaFim":"17:00:00","servicosIds":[]}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void criar_comRoleCliente_retorna403() throws Exception {
        mockMvc.perform(post("/api/escalas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profissionalVinculoId":1,"diaSemana":"SEGUNDA",
                                 "horaInicio":"09:00:00","horaFim":"17:00:00","servicosIds":[]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void atualizar_comRoleAdmin_retorna200() throws Exception {
        when(atualizarUseCase.executar(any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(escala());
        when(assembler.toResponse(any())).thenReturn(null);

        mockMvc.perform(put("/api/escalas/1")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diaSemana":"SEGUNDA","horaInicio":"09:00:00","horaFim":"17:00:00","servicosIds":[]}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void listar_comRoleProfissional_retorna200() throws Exception {
        when(listarUseCase.executar(any(), any(), any(), anyBoolean())).thenReturn(List.of());

        mockMvc.perform(get("/api/escalas")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk());
    }
}

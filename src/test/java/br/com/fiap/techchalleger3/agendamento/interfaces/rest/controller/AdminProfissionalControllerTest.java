package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CadastrarProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProfissionalController.class)
@Import(SecurityConfig.class)
class AdminProfissionalControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CadastrarProfissionalUseCase cadastrarUseCase;
    @MockitoBean ProfissionalUseCase profissionalUseCase;
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

    private Profissional profissional() {
        return Profissional.builder().id(1).nome("Dr. Carlos").email("carlos@x.com").build();
    }

    @Test
    void cadastrar_semAuth_retorna401() throws Exception {
        mockMvc.perform(post("/api/admin/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Carlos","email":"carlos@x.com","especialidades":[],"endereco":"Rua A"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastrar_comRoleAdmin_retorna201() throws Exception {
        when(cadastrarUseCase.executar(any(), any(), any(), any())).thenReturn(profissional());

        mockMvc.perform(post("/api/admin/profissionais")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Carlos","email":"carlos@x.com","especialidades":[],"endereco":"Rua A"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastrar_comRoleProfissional_retorna403() throws Exception {
        mockMvc.perform(post("/api/admin/profissionais")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Carlos","email":"carlos@x.com","especialidades":[],"endereco":"Rua A"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_comRoleAdmin_retorna200() throws Exception {
        when(profissionalUseCase.listar(any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(profissional())));

        mockMvc.perform(get("/api/admin/profissionais")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void listar_semAuth_retorna401() throws Exception {
        mockMvc.perform(get("/api/admin/profissionais"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscar_comRoleAdmin_retorna200() throws Exception {
        when(profissionalUseCase.buscarPorId(any(), anyString(), anyBoolean()))
                .thenReturn(profissional());

        mockMvc.perform(get("/api/admin/profissionais/1")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void buscar_comRoleProfissional_retorna200() throws Exception {
        when(profissionalUseCase.buscarPorId(any(), anyString(), anyBoolean()))
                .thenReturn(profissional());

        mockMvc.perform(get("/api/admin/profissionais/1")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk());
    }

    @Test
    void buscar_comRoleCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/profissionais/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void atualizar_comRoleAdmin_retorna200() throws Exception {
        when(profissionalUseCase.atualizar(any(), any(), any(), any(), anyString(), anyBoolean()))
                .thenReturn(profissional());

        mockMvc.perform(put("/api/admin/profissionais/1")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Carlos Atualizado","especialidades":[],"endereco":"Rua B"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void atualizar_comRoleProfissional_retorna200() throws Exception {
        when(profissionalUseCase.atualizar(any(), any(), any(), any(), anyString(), anyBoolean()))
                .thenReturn(profissional());

        mockMvc.perform(put("/api/admin/profissionais/1")
                        .with(jwt().jwt(j -> j.subject("sub-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Dr. Carlos Atualizado","especialidades":[],"endereco":"Rua B"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void inativar_comRoleAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/admin/profissionais/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void inativar_comRoleCliente_retorna403() throws Exception {
        mockMvc.perform(delete("/api/admin/profissionais/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }
}

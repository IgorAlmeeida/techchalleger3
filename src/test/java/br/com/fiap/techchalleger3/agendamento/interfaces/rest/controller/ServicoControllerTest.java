package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.ServicoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoController.class)
@Import(SecurityConfig.class)
class ServicoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ServicoUseCase useCase;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockitoBean ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @BeforeEach
    void filtros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarUsuarioFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private Servico servico() {
        return Servico.builder().id(1).nome("Corte").duracaoMinutos(30).preco(BigDecimal.valueOf(50)).ativo(true).build();
    }

    @Test
    void criar_retorna401_semAuth() throws Exception {
        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte","duracaoMinutos":30}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_retorna403_roleCliente() throws Exception {
        mockMvc.perform(post("/api/servicos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte","duracaoMinutos":30}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void criar_retorna201_admin() throws Exception {
        when(useCase.criar(any(), any(), any())).thenReturn(servico());

        mockMvc.perform(post("/api/servicos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte","duracaoMinutos":30,"preco":50.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Corte"));
    }

    @Test
    void listar_retorna200_admin() throws Exception {
        when(useCase.listar(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(servico())));

        mockMvc.perform(get("/api/servicos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Corte"));
    }

    @Test
    void buscar_retorna200_admin() throws Exception {
        when(useCase.buscarPorId(1)).thenReturn(servico());

        mockMvc.perform(get("/api/servicos/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void atualizar_retorna200_admin() throws Exception {
        when(useCase.atualizar(any(), any(), any(), any())).thenReturn(servico());

        mockMvc.perform(put("/api/servicos/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Corte v2","duracaoMinutos":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Corte"));
    }

    @Test
    void inativar_retorna204_admin() throws Exception {
        mockMvc.perform(delete("/api/servicos/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}

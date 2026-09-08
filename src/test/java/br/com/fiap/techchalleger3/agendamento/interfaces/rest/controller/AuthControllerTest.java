package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.*;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean LoginUseCase loginUseCase;
    @MockitoBean RenovarTokenUseCase renovarTokenUseCase;
    @MockitoBean CadastrarClienteUseCase cadastrarClienteUseCase;
    @MockitoBean RedefinirSenhaEsquecidaUseCase redefinirSenhaEsquecidaUseCase;
    @MockitoBean AlterarSenhaUseCase alterarSenhaUseCase;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockitoBean ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @BeforeEach
    void configureFiltros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarUsuarioFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private KeycloakTokenPort.TokenResponse tokenResponse() {
        return new KeycloakTokenPort.TokenResponse("access-tok", 300, "Bearer", "refresh-tok", 1800);
    }

    @Test
    void login_retorna200_comCredenciaisValidas() throws Exception {
        when(loginUseCase.executar("user@x.com", "Pass@123")).thenReturn(tokenResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user@x.com","password":"Pass@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-tok"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_retorna400_quandoCamposAusentes() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_retorna200() throws Exception {
        when(renovarTokenUseCase.executar("refresh-tok")).thenReturn(tokenResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh-tok"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh-tok"));
    }

    @Test
    void cadastrarCliente_retorna201() throws Exception {
        Cliente cliente = Cliente.builder().id(1).nome("João").build();
        when(cadastrarClienteUseCase.executar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cliente);

        mockMvc.perform(post("/api/auth/cadastrar-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome":"João","email":"joao@x.com","password":"Senha@123",
                                  "cpf":"111.222.333-44","dataNascimento":"1990-01-01",
                                  "telefone":"11999999999","sexo":"M","endereco":"Rua X"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void esqueciSenha_retorna200_semRevealEmail() throws Exception {
        mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"x@x.com"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void alterarSenha_retorna4xx_semAutenticacao() throws Exception {
        // Spring Security pode retornar 401 ou 403 dependendo da config
        mockMvc.perform(patch("/api/auth/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual":"Old@1","senhaNova":"New@123"}
                                """))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void alterarSenha_retorna200_comAutenticacao() throws Exception {
        mockMvc.perform(patch("/api/auth/alterar-senha")
                        .with(jwt().jwt(j -> j.subject("kc-sub").claim("email", "u@x.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual":"Old@1","senhaNova":"New@123"}
                                """))
                .andExpect(status().isOk());
    }
}

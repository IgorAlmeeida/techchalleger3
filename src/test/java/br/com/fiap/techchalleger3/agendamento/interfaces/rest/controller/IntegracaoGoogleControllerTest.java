package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.IntegracaoCalendarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.calendario.GoogleOAuthHelper;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import com.google.api.client.auth.oauth2.TokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntegracaoGoogleController.class)
@Import(SecurityConfig.class)
class IntegracaoGoogleControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GoogleOAuthHelper oAuthHelper;
    @MockitoBean private IntegracaoCalendarioRepositoryPort integracaoPort;
    @MockitoBean private UsuarioRepositoryPort usuarioPort;
    @MockitoBean private ClienteRepositoryPort clientePort;
    @MockitoBean private ProfissionalRepositoryPort profissionalPort;
    @MockitoBean private ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    @BeforeEach
    void configureFiltros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void autorizar_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/integracoes/google/autorizar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void autorizar_comRoleCliente_retorna200() throws Exception {
        when(oAuthHelper.gerarUrlAutorizacao(anyString()))
                .thenReturn("https://accounts.google.com/o/oauth2/auth?state=user-sub");

        mockMvc.perform(get("/api/integracoes/google/autorizar")
                        .with(jwt().jwt(j -> j.subject("user-sub"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk());
    }

    @Test
    void autorizar_comRoleProfissional_retorna200() throws Exception {
        when(oAuthHelper.gerarUrlAutorizacao(anyString()))
                .thenReturn("https://accounts.google.com/o/oauth2/auth?state=prof-sub");

        mockMvc.perform(get("/api/integracoes/google/autorizar")
                        .with(jwt().jwt(j -> j.subject("prof-sub"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isOk());
    }

    @Test
    void callback_usuarioCliente_retorna200() throws Exception {
        Usuario usuario = Usuario.builder().id(1).uuid("user-sub").role(RoleEnum.CLIENTE).build();
        Cliente cliente = Cliente.builder().id(10).build();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");
        tokenResponse.setExpiresInSeconds(3600L);

        when(usuarioPort.buscarPorUuid("user-sub")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente));
        when(oAuthHelper.trocarCodigoPorTokens("auth-code")).thenReturn(tokenResponse);

        mockMvc.perform(get("/api/integracoes/google/callback")
                        .param("code", "auth-code")
                        .param("state", "user-sub"))
                .andExpect(status().isOk());
    }

    @Test
    void callback_usuarioProfissional_retorna200() throws Exception {
        Usuario usuario = Usuario.builder().id(2).uuid("prof-sub").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(20).build();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");

        when(usuarioPort.buscarPorUuid("prof-sub")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(2)).thenReturn(Optional.of(profissional));
        when(oAuthHelper.trocarCodigoPorTokens("auth-code")).thenReturn(tokenResponse);

        mockMvc.perform(get("/api/integracoes/google/callback")
                        .param("code", "auth-code")
                        .param("state", "prof-sub"))
                .andExpect(status().isOk());
    }

    @Test
    void callback_semExpiresIn_retorna200() throws Exception {
        Usuario usuario = Usuario.builder().id(1).uuid("user-sub").role(RoleEnum.CLIENTE).build();
        Cliente cliente = Cliente.builder().id(10).build();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("access-token");
        // expiresInSeconds = null intencionalmente

        when(usuarioPort.buscarPorUuid("user-sub")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente));
        when(oAuthHelper.trocarCodigoPorTokens("auth-code")).thenReturn(tokenResponse);

        mockMvc.perform(get("/api/integracoes/google/callback")
                        .param("code", "auth-code")
                        .param("state", "user-sub"))
                .andExpect(status().isOk());
    }

    @Test
    void remover_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(delete("/api/integracoes/google"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void remover_comRoleCliente_retorna204() throws Exception {
        Usuario usuario = Usuario.builder().id(1).uuid("user-sub").role(RoleEnum.CLIENTE).build();
        Cliente cliente = Cliente.builder().id(10).build();

        when(usuarioPort.buscarPorUuid("user-sub")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente));

        mockMvc.perform(delete("/api/integracoes/google")
                        .with(jwt().jwt(j -> j.subject("user-sub"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void remover_comRoleProfissional_retorna204() throws Exception {
        Usuario usuario = Usuario.builder().id(2).uuid("prof-sub").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(20).build();

        when(usuarioPort.buscarPorUuid("prof-sub")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(2)).thenReturn(Optional.of(profissional));

        mockMvc.perform(delete("/api/integracoes/google")
                        .with(jwt().jwt(j -> j.subject("prof-sub"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))))
                .andExpect(status().isNoContent());
    }
}

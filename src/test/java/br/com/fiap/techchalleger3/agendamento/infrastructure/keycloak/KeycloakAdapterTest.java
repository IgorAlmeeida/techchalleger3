package br.com.fiap.techchalleger3.agendamento.infrastructure.keycloak;

import br.com.fiap.techchalleger3.agendamento.domain.exception.EmailJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ServicoIndisponivelException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakAdapterTest {

    // ── KeycloakAdminAdapter ───────────────────────────────────────────────

    private KeycloakAdminAdapter adminAdapter;
    private Keycloak kcMock;

    @BeforeEach
    void setup() {
        adminAdapter = new KeycloakAdminAdapter("http://kc", "testrealm", "client", "secret");
        kcMock = mock(Keycloak.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(adminAdapter, "keycloak", kcMock);
    }

    @Test
    void criarUsuario_sucesso() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location"))
                .thenReturn("http://kc/admin/realms/testrealm/users/user-abc-123");

        when(kcMock.realm("testrealm").users().create(any())).thenReturn(response);

        RoleRepresentation roleRep = new RoleRepresentation();
        when(kcMock.realm("testrealm").roles().get("CLIENTE").toRepresentation()).thenReturn(roleRep);

        String userId = adminAdapter.criarUsuario("x@x.com", "João", "Senha@1", "CLIENTE", true);

        assertThat(userId).isEqualTo("user-abc-123");
    }

    @Test
    void criarUsuario_409_lancaEmailJaCadastrado() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(409);
        when(kcMock.realm("testrealm").users().create(any())).thenReturn(response);

        assertThatThrownBy(() -> adminAdapter.criarUsuario("x@x.com", "João", "Senha@1", "CLIENTE", true))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void criarUsuario_500_lancaServicoIndisponivel() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);
        when(kcMock.realm("testrealm").users().create(any())).thenReturn(response);

        assertThatThrownBy(() -> adminAdapter.criarUsuario("x@x.com", "João", "Senha@1", "CLIENTE", true))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    void criarUsuario_excecaoGenerica_lancaServicoIndisponivel() {
        when(kcMock.realm("testrealm").users().create(any())).thenThrow(new RuntimeException("Keycloak down"));

        assertThatThrownBy(() -> adminAdapter.criarUsuario("x@x.com", "João", "Senha@1", "CLIENTE", true))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    void redefinirSenha_sucesso() {
        adminAdapter.redefinirSenha("user-123", "NovaSenha@1", false);
        // no exception = success
    }

    @Test
    void redefinirSenha_excecao_lancaServicoIndisponivel() {
        when(kcMock.realm("testrealm").users().get("user-123")).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> adminAdapter.redefinirSenha("user-123", "NovaSenha@1", false))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    // ── KeycloakTokenAdapter ───────────────────────────────────────────────

    private KeycloakTokenAdapter tokenAdapter;

    @BeforeEach
    void setupToken() {
        tokenAdapter = new KeycloakTokenAdapter("http://kc", "testrealm", "client", "secret");
    }

    @Test
    void obterToken_servidorInacessivel_lancaServicoIndisponivel() {
        // KeycloakBuilder cria cliente real para http://kc — host não resolve → ServicoIndisponivelException
        assertThatThrownBy(() -> tokenAdapter.obterToken("user@x.com", "wrong"))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    @Test
    void renovarToken_excecaoGenerica_lancaServicoIndisponivel() {
        // O chain de mock do RestClient é hard to set up em Java 21 com final fields;
        // a exception genérica é capturada antes de chegar ao body() → ServicoIndisponivelException
        assertThatThrownBy(() -> tokenAdapter.renovarToken("ref"))
                .isInstanceOf(ServicoIndisponivelException.class);
    }
}

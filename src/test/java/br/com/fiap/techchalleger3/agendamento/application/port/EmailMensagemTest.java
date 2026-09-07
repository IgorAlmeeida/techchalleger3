package br.com.fiap.techchalleger3.agendamento.application.port;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakTokenPortTest {

    @Test
    void tokenResponse_acessoresRetornamValoresCorretos() {
        KeycloakTokenPort.TokenResponse resp = new KeycloakTokenPort.TokenResponse(
                "access-abc", 300, "Bearer", "refresh-xyz", 1800);

        assertThat(resp.accessToken()).isEqualTo("access-abc");
        assertThat(resp.expiresIn()).isEqualTo(300);
        assertThat(resp.tokenType()).isEqualTo("Bearer");
        assertThat(resp.refreshToken()).isEqualTo("refresh-xyz");
        assertThat(resp.refreshExpiresIn()).isEqualTo(1800);
    }

    @Test
    void tokenResponse_equalsEHashCode() {
        KeycloakTokenPort.TokenResponse a = new KeycloakTokenPort.TokenResponse("tok", 1, "Bearer", "ref", 2);
        KeycloakTokenPort.TokenResponse b = new KeycloakTokenPort.TokenResponse("tok", 1, "Bearer", "ref", 2);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void tokenResponse_toString() {
        KeycloakTokenPort.TokenResponse r = new KeycloakTokenPort.TokenResponse("tok", 1, "Bearer", "ref", 2);

        assertThat(r.toString()).contains("tok");
    }
}

class EmailMensagemTest {

    @Test
    void acessoresRetornamValoresCorretos() {
        Map<String, Object> dados = Map.of("nome", "João");
        EmailMensagem msg = new EmailMensagem("dest@x.com", "TEMPLATE", dados);

        assertThat(msg.destinatario()).isEqualTo("dest@x.com");
        assertThat(msg.template()).isEqualTo("TEMPLATE");
        assertThat(msg.dados()).isEqualTo(dados);
    }

    @Test
    void equalsEHashCode() {
        Map<String, Object> dados = Map.of("k", "v");
        EmailMensagem a = new EmailMensagem("a@b.com", "T", dados);
        EmailMensagem b = new EmailMensagem("a@b.com", "T", dados);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toString_contem_destinatario() {
        EmailMensagem msg = new EmailMensagem("dest@x.com", "T", Map.of());

        assertThat(msg.toString()).contains("dest@x.com");
    }
}

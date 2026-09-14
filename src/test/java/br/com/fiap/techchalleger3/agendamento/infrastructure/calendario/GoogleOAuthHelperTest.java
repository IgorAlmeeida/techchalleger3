package br.com.fiap.techchalleger3.agendamento.infrastructure.calendario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleOAuthHelperTest {

    private GoogleOAuthHelper helper;

    @BeforeEach
    void setUp() {
        helper = new GoogleOAuthHelper();
        ReflectionTestUtils.setField(helper, "clientId", "test-client-id");
        ReflectionTestUtils.setField(helper, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(helper, "redirectUri", "http://localhost/callback");
    }

    @Test
    void gerarUrlAutorizacao_retornaUrlValida() {
        String url = helper.gerarUrlAutorizacao("user-state-123");

        assertThat(url).isNotBlank();
        assertThat(url).contains("accounts.google.com");
        assertThat(url).contains("user-state-123");
        assertThat(url).contains("offline");
    }

    @Test
    void gerarUrlAutorizacao_incluiRedirectUri() {
        String url = helper.gerarUrlAutorizacao("any-state");

        assertThat(url).contains("redirect_uri");
    }

    @Test
    void trocarCodigoPorTokens_codigoInvalido_lancaIllegalStateException() {
        assertThatThrownBy(() -> helper.trocarCodigoPorTokens("invalid-code"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Falha ao trocar código por tokens Google");
    }
}

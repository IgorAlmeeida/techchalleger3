package br.com.fiap.techchalleger3.agendamento.infrastructure.keycloak;

import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ServicoIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakTokenAdapterTest {

    private KeycloakTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new KeycloakTokenAdapter(
                "http://localhost:8080",
                "test-realm",
                "test-client",
                "test-secret");
    }

    // ── toInt via reflexão: cobre 4 branches ─────────────────────────────────

    @Test
    void toInt_integer() throws Exception {
        assertThat(invokeToInt(300)).isEqualTo(300);
    }

    @Test
    void toInt_long() throws Exception {
        assertThat(invokeToInt(300L)).isEqualTo(300);
    }

    @Test
    void toInt_double() throws Exception {
        // Double implements Number but not Integer/Long → third branch
        assertThat(invokeToInt(300.0)).isEqualTo(300);
    }

    @Test
    void toInt_null_retornaZero() throws Exception {
        assertThat(invokeToInt(null)).isEqualTo(0);
    }

    // ── renovarToken: Unauthorized → CredenciaisInvalidas ─────────────────────

    @Test
    void renovarToken_unauthorized_lancaCredenciaisInvalidas() {
        HttpClientErrorException.Unauthorized ex = mock(HttpClientErrorException.Unauthorized.class);
        stubRestClientThrows(ex);

        assertThatThrownBy(() -> adapter.renovarToken("bad-refresh"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    // ── renovarToken: Exception genérica → ServicoIndisponivel ───────────────

    @Test
    void renovarToken_excecaoGenerica_lancaServicoIndisponivel() {
        stubRestClientThrows(new RuntimeException("timeout"));

        assertThatThrownBy(() -> adapter.renovarToken("bad-refresh"))
                .isInstanceOf(ServicoIndisponivelException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private int invokeToInt(Object value) throws Exception {
        Method m = KeycloakTokenAdapter.class.getDeclaredMethod("toInt", Object.class);
        m.setAccessible(true);
        return (int) m.invoke(adapter, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubRestClientThrows(RuntimeException ex) {
        RestClient mockRestClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);

        when(mockRestClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Object[].class))).thenThrow(ex);

        ReflectionTestUtils.setField(adapter, "restClient", mockRestClient);
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.calendario;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class GoogleCalendarioAdapterTest {

    private GoogleCalendarioAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoogleCalendarioAdapter();
        ReflectionTestUtils.setField(adapter, "clientId", "test-client-id");
        ReflectionTestUtils.setField(adapter, "clientSecret", "test-client-secret");
    }

    private Agendamento agendamento() {
        return Agendamento.builder()
                .id(1)
                .dataAgenda(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(10, 0))
                .build();
    }

    private IntegracaoCalendarioExterno integracao() {
        return IntegracaoCalendarioExterno.builder()
                .accessToken("fake-access-token")
                .refreshToken("fake-refresh-token")
                .build();
    }

    @Test
    void criarEvento_falhaAoConectarGoogle_retornaNull() {
        // O token é inválido — a chamada ao Google falha e o catch retorna null
        String resultado = adapter.criarEvento(agendamento(), integracao());

        assertThat(resultado).isNull();
    }

    @Test
    void removerEvento_googleEventIdNulo_naoLancaExcecao() {
        assertThatNoException().isThrownBy(() ->
                adapter.removerEvento(null, integracao()));
    }

    @Test
    void removerEvento_googleEventIdVazio_naoLancaExcecao() {
        assertThatNoException().isThrownBy(() ->
                adapter.removerEvento("", integracao()));
    }

    @Test
    void removerEvento_googleEventIdEmBranco_naoLancaExcecao() {
        assertThatNoException().isThrownBy(() ->
                adapter.removerEvento("   ", integracao()));
    }

    @Test
    void removerEvento_idValido_falhaAoConectarGoogle_naoLancaExcecao() {
        // Token inválido → exceção capturada internamente com log.error
        assertThatNoException().isThrownBy(() ->
                adapter.removerEvento("google-event-id-123", integracao()));
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AsyncConfigTest {

    private final AsyncConfig config = new AsyncConfig();

    @Test
    void emailTaskExecutor_temConfiguracaoEsperada() {
        Executor executor = config.emailTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor tpte = (ThreadPoolTaskExecutor) executor;
        assertThat(tpte.getCorePoolSize()).isEqualTo(2);
        assertThat(tpte.getMaxPoolSize()).isEqualTo(5);
        assertThat(tpte.getThreadNamePrefix()).isEqualTo("email-async-");
    }

    @Test
    void calendarTaskExecutor_temConfiguracaoEsperada() {
        Executor executor = config.calendarTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor tpte = (ThreadPoolTaskExecutor) executor;
        assertThat(tpte.getCorePoolSize()).isEqualTo(2);
        assertThat(tpte.getMaxPoolSize()).isEqualTo(5);
        assertThat(tpte.getThreadNamePrefix()).isEqualTo("calendar-async-");
    }

    @Test
    void asyncUncaughtExceptionHandler_naoLancaAoTratarErro() throws NoSuchMethodException {
        var handler = config.getAsyncUncaughtExceptionHandler();
        var method = AsyncConfigTest.class.getDeclaredMethod("asyncUncaughtExceptionHandler_naoLancaAoTratarErro");

        assertThatNoException().isThrownBy(() ->
                handler.handleUncaughtException(new RuntimeException("erro simulado"), method));
    }
}

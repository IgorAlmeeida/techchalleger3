package br.com.fiap.techchalleger3.agendamento;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requer Testcontainers com Postgres/Redis/RabbitMQ/Keycloak — use IntegrationTestBase para testes de contexto completo")
@SpringBootTest
class AgendamentoApplicationTests {

    @Test
    void contextLoads() {
    }

}

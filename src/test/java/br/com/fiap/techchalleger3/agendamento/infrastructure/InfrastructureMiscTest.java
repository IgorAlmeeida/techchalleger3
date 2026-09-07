package br.com.fiap.techchalleger3.agendamento.infrastructure;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.adapter.IcsCalendarioExportAdapter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.cache.RedisCachePortImpl;
import br.com.fiap.techchalleger3.agendamento.infrastructure.email.RabbitEmailSenderPortImpl;
import br.com.fiap.techchalleger3.agendamento.infrastructure.scheduler.FecharAgendamentosJob;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.KeycloakRolesConverter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SenhaTemporariaGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfrastructureMiscTest {

    // ── FecharAgendamentosJob ─────────────────────────────────────────────

    @Mock AgendamentoRepositoryPort agendamentoPort;
    @InjectMocks FecharAgendamentosJob fecharJob;

    @Test
    void fecharAgendamentos_listaVazia() {
        when(agendamentoPort.buscarAgendadosPassados()).thenReturn(List.of());

        fecharJob.fecharAgendamentosPassados();

        verify(agendamentoPort, never()).salvar(any());
    }

    @Test
    void fecharAgendamentos_apenasFilhos_ignorados() {
        Agendamento filho = Agendamento.builder().id(2).agendamentoPaiId(1)
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
        when(agendamentoPort.buscarAgendadosPassados()).thenReturn(List.of(filho));

        fecharJob.fecharAgendamentosPassados();

        verify(agendamentoPort, never()).salvar(any());
    }

    @Test
    void fecharAgendamentos_paiComPresencaConfirmada_salvaComoRealizado() {
        Agendamento pai = Agendamento.builder().id(1).agendamentoPaiId(null)
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(true).build();
        when(agendamentoPort.buscarAgendadosPassados()).thenReturn(List.of(pai));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        fecharJob.fecharAgendamentosPassados();

        assertThat(pai.getStatus()).isEqualTo(StatusAgendamentoEnum.REALIZADO);
        verify(agendamentoPort).salvar(pai);
    }

    @Test
    void fecharAgendamentos_paiSemPresenca_salvaComoNaoRealizado() {
        Agendamento pai = Agendamento.builder().id(1).agendamentoPaiId(null)
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
        Agendamento filho = Agendamento.builder().id(2).agendamentoPaiId(1)
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
        when(agendamentoPort.buscarAgendadosPassados()).thenReturn(List.of(pai));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of(filho));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        fecharJob.fecharAgendamentosPassados();

        assertThat(pai.getStatus()).isEqualTo(StatusAgendamentoEnum.NAO_REALIZADO);
        assertThat(filho.getStatus()).isEqualTo(StatusAgendamentoEnum.NAO_REALIZADO);
        verify(agendamentoPort, times(2)).salvar(any());
    }

    // ── RedisCachePortImpl ────────────────────────────────────────────────

    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOps;
    @InjectMocks RedisCachePortImpl redisCachePort;

    @Test
    void redis_put() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        redisCachePort.put("key", "value", Duration.ofMinutes(10));

        verify(valueOps).set("key", "value", Duration.ofMinutes(10));
    }

    @Test
    void redis_get_encontrado() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("key")).thenReturn("cached");

        Optional<String> result = redisCachePort.get("key", String.class);

        assertThat(result).contains("cached");
    }

    @Test
    void redis_get_ausente() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("missing")).thenReturn(null);

        Optional<String> result = redisCachePort.get("missing", String.class);

        assertThat(result).isEmpty();
    }

    @Test
    void redis_invalidar() {
        redisCachePort.invalidar("chave");

        verify(redisTemplate).delete("chave");
    }

    // ── RabbitEmailSenderPortImpl ─────────────────────────────────────────

    @Mock RabbitTemplate rabbitTemplate;

    @Test
    void email_enviar() {
        RabbitEmailSenderPortImpl emailSender = new RabbitEmailSenderPortImpl(rabbitTemplate, "agendamento.email");
        EmailMensagem mensagem = new EmailMensagem("dest@x.com", "RESET_SENHA", Map.of("senha", "tmp123"));

        emailSender.enviar(mensagem);

        verify(rabbitTemplate).convertAndSend("agendamento.email", mensagem);
    }

    // ── IcsCalendarioExportAdapter ────────────────────────────────────────

    private final IcsCalendarioExportAdapter icsAdapter = new IcsCalendarioExportAdapter();

    @Test
    void ics_exportar_conteudoValido() {
        Agendamento ag = Agendamento.builder().id(42).dataAgenda(LocalDate.of(2026, 9, 10))
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).build();

        byte[] result = icsAdapter.exportarIcs(ag);

        String ics = new String(result);
        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("agendamento-42@agendamento.app");
        assertThat(ics).contains("20260910T090000");
        assertThat(ics).contains("20260910T100000");
        assertThat(ics).contains("END:VCALENDAR");
    }

    // ── KeycloakRolesConverter ────────────────────────────────────────────

    private final KeycloakRolesConverter rolesConverter = new KeycloakRolesConverter();

    @Test
    void keycloakRoles_semRealmAccess_retornaVazio() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsMap("realm_access")).thenReturn(null);

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).isEmpty();
    }

    @Test
    void keycloakRoles_semRoles_retornaVazio() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsMap("realm_access")).thenReturn(Map.of());

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).isEmpty();
    }

    @Test
    void keycloakRoles_comRoles_retornaAuthorities() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsMap("realm_access")).thenReturn(Map.of("roles", List.of("ADMIN", "PROFISSIONAL")));

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).hasSize(2);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PROFISSIONAL");
    }

    // ── SenhaTemporariaGenerator ──────────────────────────────────────────

    @Test
    void senha_gerar_tamanho12() {
        String senha = SenhaTemporariaGenerator.gerar();

        assertThat(senha).hasSize(12);
    }

    @Test
    void senha_gerar_conteudoMisto() {
        // Gera 20 senhas e verifica que variam (não são determinísticas)
        String s1 = SenhaTemporariaGenerator.gerar();
        String s2 = SenhaTemporariaGenerator.gerar();

        // Ambas devem ter 12 chars e conter mix de caracteres
        assertThat(s1).hasSize(12);
        assertThat(s2).hasSize(12);
        // Garante que ao menos uma letra maiúscula, minúscula, dígito ou especial existe
        assertThat(s1).matches(".*[A-Z].*|.*[a-z].*|.*[0-9].*|.*[@#$!].*");
    }
}

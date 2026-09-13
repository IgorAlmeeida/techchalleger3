package br.com.fiap.techchalleger3.agendamento.infrastructure;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.adapter.IcsCalendarioExportAdapter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.email.AsyncEmailSenderPortImpl;
import br.com.fiap.techchalleger3.agendamento.infrastructure.scheduler.FecharAgendamentosJob;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.AppJwtRolesConverter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SenhaTemporariaGenerator;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    // ── AsyncEmailSenderPortImpl ──────────────────────────────────────────

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;
    @InjectMocks AsyncEmailSenderPortImpl asyncEmailSender;

    @Test
    void email_enviar_chamaSend() throws Exception {
        EmailMensagem mensagem = new EmailMensagem("dest@x.com", "RESET_SENHA", Map.of("senha", "tmp123"));
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("RESET_SENHA"), any(Context.class))).thenReturn("<html>ok</html>");

        asyncEmailSender.enviar(mensagem);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void email_enviar_erroNaoPropagarExcecao() {
        EmailMensagem mensagem = new EmailMensagem("dest@x.com", "RESET_SENHA", Map.of());
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        asyncEmailSender.enviar(mensagem);
        // sem exceção propagada — erro logado internamente
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
        assertThat(ics)
                .contains("BEGIN:VCALENDAR")
                .contains("BEGIN:VEVENT")
                .contains("agendamento-42@agendamento.app")
                .contains("20260910T090000")
                .contains("20260910T100000")
                .contains("END:VCALENDAR");
    }

    // ── AppJwtRolesConverter ──────────────────────────────────────────────

    private final AppJwtRolesConverter rolesConverter = new AppJwtRolesConverter();

    @Test
    void jwtRoles_semRoleClaim_retornaVazio() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("role")).thenReturn(null);

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).isEmpty();
    }

    @Test
    void jwtRoles_comRole_retornaAuthority() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("role")).thenReturn("ADMIN");

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void jwtRoles_comRoleCliente_retornaAuthority() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("role")).thenReturn("CLIENTE");

        Collection<GrantedAuthority> authorities = rolesConverter.convert(jwt);

        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_CLIENTE");
    }

    // ── SenhaTemporariaGenerator ──────────────────────────────────────────

    @Test
    void senha_gerar_tamanho12() {
        String senha = SenhaTemporariaGenerator.gerar();

        assertThat(senha).hasSize(12);
    }

    @Test
    void senha_gerar_conteudoMisto() {
        String s1 = SenhaTemporariaGenerator.gerar();
        String s2 = SenhaTemporariaGenerator.gerar();

        assertThat(s1).hasSize(12);
        assertThat(s2).hasSize(12);
        assertThat(s1).matches(".*[A-Z].*|.*[a-z].*|.*[0-9].*|.*[@#$!].*");
    }
}

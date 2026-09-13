package br.com.fiap.techchalleger3.agendamento.infrastructure.calendario;

import br.com.fiap.techchalleger3.agendamento.application.port.CalendarioExternoGateway;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleCalendarioAdapter implements CalendarioExternoGateway {

    @Value("${google.calendar.client-id}")
    private String clientId;

    @Value("${google.calendar.client-secret}")
    private String clientSecret;

    private static final String APPLICATION_NAME = "Agendamento-TechChallenge3";
    private static final String CALENDAR_ID = "primary";

    @Override
    public String criarEvento(Agendamento agendamento, IntegracaoCalendarioExterno integracao) {
        try {
            Calendar service = buildCalendarService(integracao);
            LocalDate data = agendamento.getDataAgenda();
            ZoneId zone = ZoneId.systemDefault();

            Event evento = new Event()
                    .setSummary("Agendamento #" + agendamento.getId())
                    .setDescription("Serviço agendado — ID " + agendamento.getId());

            DateTime inicio = new DateTime(
                    data.atTime(agendamento.getHoraInicio()).atZone(zone).toInstant().toEpochMilli());
            DateTime fim = new DateTime(
                    data.atTime(agendamento.getHoraFim()).atZone(zone).toInstant().toEpochMilli());

            evento.setStart(new EventDateTime().setDateTime(inicio).setTimeZone(zone.getId()));
            evento.setEnd(new EventDateTime().setDateTime(fim).setTimeZone(zone.getId()));

            Event criado = service.events().insert(CALENDAR_ID, evento).execute();
            return criado.getId();
        } catch (Exception e) {
            log.error("Falha ao criar evento no Google Calendar para agendamento {}: {}",
                    agendamento.getId(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void removerEvento(String googleEventId, IntegracaoCalendarioExterno integracao) {
        if (googleEventId == null || googleEventId.isBlank()) return;
        try {
            Calendar service = buildCalendarService(integracao);
            service.events().delete(CALENDAR_ID, googleEventId).execute();
        } catch (Exception e) {
            log.error("Falha ao remover evento {} do Google Calendar: {}", googleEventId, e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private Calendar buildCalendarService(IntegracaoCalendarioExterno integracao)
            throws GeneralSecurityException, IOException {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .build()
                .setAccessToken(integracao.getAccessToken())
                .setRefreshToken(integracao.getRefreshToken());
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

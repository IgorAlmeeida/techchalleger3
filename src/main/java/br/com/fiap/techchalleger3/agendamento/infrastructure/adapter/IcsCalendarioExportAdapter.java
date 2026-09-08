package br.com.fiap.techchalleger3.agendamento.infrastructure.adapter;

import br.com.fiap.techchalleger3.agendamento.application.port.CalendarioExportPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Component
public class IcsCalendarioExportAdapter implements CalendarioExportPort {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    public byte[] exportarIcs(Agendamento agendamento) {
        LocalDate data = agendamento.getDataAgenda();
        LocalTime inicio = agendamento.getHoraInicio();
        LocalTime fim = agendamento.getHoraFim();

        String dtStart = data.atTime(inicio).format(DT_FMT);
        String dtEnd   = data.atTime(fim).format(DT_FMT);
        String uid     = "agendamento-" + agendamento.getId() + "@agendamento.app";
        String now     = java.time.LocalDateTime.now(ZoneId.systemDefault()).format(DT_FMT);

        String ics = "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//AgendaFacil//Agendamento//PT\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTAMP:" + now + "\r\n" +
                "DTSTART:" + dtStart + "\r\n" +
                "DTEND:" + dtEnd + "\r\n" +
                "SUMMARY:Agendamento #" + agendamento.getId() + "\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";

        return ics.getBytes(StandardCharsets.UTF_8);
    }
}

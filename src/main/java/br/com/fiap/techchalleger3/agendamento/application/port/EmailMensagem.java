package br.com.fiap.techchalleger3.agendamento.application.port;

import java.util.Map;

public record EmailMensagem(
        String destinatario,
        String template,
        Map<String, Object> dados
) {}

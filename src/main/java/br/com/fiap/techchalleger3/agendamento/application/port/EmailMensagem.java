package br.com.fiap.techchalleger3.agendamento.application.port;

import java.util.Map;

/**
 * Modelo de mensagem de e-mail enviada pela porta {@link EmailSenderPort}.
 * Contém destinatário, template Thymeleaf e dados de preenchimento.
 */
public record EmailMensagem(
        String destinatario,
        String template,
        Map<String, Object> dados
) {}

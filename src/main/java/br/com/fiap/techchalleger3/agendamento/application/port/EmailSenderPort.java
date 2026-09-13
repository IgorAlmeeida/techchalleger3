package br.com.fiap.techchalleger3.agendamento.application.port;

/**
 * Porta de saída para envio de notificações por e-mail. Use cases dependem desta interface —
 * implementação concreta (SMTP via Spring Mail) pode ser trocada sem alterar casos de uso.
 */
public interface EmailSenderPort {
    void enviar(EmailMensagem mensagem);
}

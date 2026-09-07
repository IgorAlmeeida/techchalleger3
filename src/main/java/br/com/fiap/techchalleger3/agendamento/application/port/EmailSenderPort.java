package br.com.fiap.techchalleger3.agendamento.application.port;

public interface EmailSenderPort {
    void enviar(EmailMensagem mensagem);
}

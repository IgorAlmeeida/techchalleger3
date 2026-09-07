package br.com.fiap.techchalleger3.agendamento.infrastructure.email;

import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitEmailSenderPortImpl implements EmailSenderPort {

    private final RabbitTemplate rabbitTemplate;
    private final String filaEmail;

    public RabbitEmailSenderPortImpl(
            RabbitTemplate rabbitTemplate,
            @Value("${agendamento.email.fila:agendamento.email}") String filaEmail) {
        this.rabbitTemplate = rabbitTemplate;
        this.filaEmail = filaEmail;
    }

    @Override
    public void enviar(EmailMensagem mensagem) {
        rabbitTemplate.convertAndSend(filaEmail, mensagem);
    }
}

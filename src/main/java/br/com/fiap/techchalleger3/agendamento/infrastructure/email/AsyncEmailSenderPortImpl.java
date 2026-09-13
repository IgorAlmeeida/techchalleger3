package br.com.fiap.techchalleger3.agendamento.infrastructure.email;

import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEmailSenderPortImpl implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async("emailTaskExecutor")
    public void enviar(EmailMensagem mensagem) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(mensagem.destinatario());
            helper.setSubject(mensagem.template());

            Context contexto = new Context();
            contexto.setVariables(mensagem.dados());
            String html = templateEngine.process(mensagem.template(), contexto);
            helper.setText(html, true);

            mailSender.send(mime);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", mensagem.destinatario(), e.getMessage(), e);
        }
    }
}

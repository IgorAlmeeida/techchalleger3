package br.com.fiap.techchalleger3.agendamento.interfaces.rest;

import br.com.fiap.techchalleger3.agendamento.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleNaoEncontrado() {
        ResponseEntity<?> r = handler.handleNaoEncontrado(new RegistroNaoEncontradoException("Servico", 1));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleAgendaEmAberto() {
        ResponseEntity<?> r = handler.handleAgendaEmAberto(new AgendaEmAbertoException("msg"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleAgendamentoJaExistente() {
        ResponseEntity<?> r = handler.handleAgendamentoJaExistente(new AgendamentoJaExistenteException("msg"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleConflitoHorario() {
        ResponseEntity<?> r = handler.handleConflitoHorario(new ConflitoDeHorarioClienteException("msg"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleDomain() {
        ResponseEntity<?> r = handler.handleDomain(new OperacaoInvalidaException("msg"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleAcesso() {
        ResponseEntity<?> r = handler.handleAcesso(new AcessoNegadoException("msg"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleAcessoNegadoSpringSecurity() {
        ResponseEntity<?> r = handler.handleAcessoNegadoSpringSecurity(
                new org.springframework.security.access.AccessDeniedException("denied"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleCredenciaisInvalidas() {
        ResponseEntity<?> r = handler.handleCredenciaisInvalidas(new CredenciaisInvalidasException());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleEmailJaCadastrado() {
        ResponseEntity<?> r = handler.handleEmailJaCadastrado(new EmailJaCadastradoException("x@x.com"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleServicoIndisponivel() {
        ResponseEntity<?> r = handler.handleServicoIndisponivel(new ServicoIndisponivelException("Keycloak"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleGenerico() {
        ResponseEntity<?> r = handler.handleGenerico(new RuntimeException("oops"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

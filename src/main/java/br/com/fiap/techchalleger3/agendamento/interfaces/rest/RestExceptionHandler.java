package br.com.fiap.techchalleger3.agendamento.interfaces.rest;

import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendaEmAbertoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ConflitoDeHorarioClienteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.DomainException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.EmailJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    public record ErroResponse(
            String codigo,
            String mensagem,
            List<String> violacoes,
            LocalDateTime timestamp
    ) {}

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RegistroNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("NAO_ENCONTRADO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(AgendaEmAbertoException.class)
    public ResponseEntity<ErroResponse> handleAgendaEmAberto(AgendaEmAbertoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("AGENDA_EM_ABERTO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(AgendamentoJaExistenteException.class)
    public ResponseEntity<ErroResponse> handleAgendamentoJaExistente(AgendamentoJaExistenteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("AGENDAMENTO_JA_EXISTENTE", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(ConflitoDeHorarioClienteException.class)
    public ResponseEntity<ErroResponse> handleConflitoHorario(ConflitoDeHorarioClienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("CONFLITO_DE_HORARIO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErroResponse> handleDomain(DomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("REGRA_NEGOCIO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> handleAcesso(AcessoNegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResponse("ACESSO_NEGADO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegadoSpringSecurity(
            org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResponse("ACESSO_NEGADO", "Acesso negado.", List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResponse("CREDENCIAIS_INVALIDAS", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("EMAIL_JA_CADASTRADO", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleServicoIndisponivel(ServicoIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse("SERVICO_INDISPONIVEL", ex.getMessage(), List.of(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        log.error("Erro inesperado capturado pelo handler: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse("ERRO_INTERNO", "Erro inesperado", List.of(), LocalDateTime.now()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> violacoes = ex.getBindingResult().getAllErrors().stream()
                .map(e -> e instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : e.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse("VALIDACAO", "Requisição inválida", violacoes, LocalDateTime.now()));
    }
}

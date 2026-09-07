package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos");
    }
}

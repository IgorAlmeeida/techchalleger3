package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando as credenciais informadas no login não são válidas.
 */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos");
    }
}

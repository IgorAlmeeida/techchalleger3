package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada ao tentar cadastrar um e-mail que já está associado a outro usuário.
 */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}

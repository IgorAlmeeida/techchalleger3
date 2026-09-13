package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando um novo item de escala conflita com outro já cadastrado para o mesmo profissional.
 */
public class ConflitoDeEscalaException extends DomainException {
    public ConflitoDeEscalaException(String mensagem) {
        super(mensagem);
    }
}

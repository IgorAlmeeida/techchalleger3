package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Exceção base de domínio. Todas as exceções de regra de negócio estendem esta classe.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String mensagem) {
        super(mensagem);
    }
}

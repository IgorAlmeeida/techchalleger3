package br.com.fiap.techchalleger3.agendamento.domain.exception;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String mensagem) {
        super(mensagem);
    }
}

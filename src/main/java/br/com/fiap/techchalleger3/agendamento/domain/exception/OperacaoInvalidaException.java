package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class OperacaoInvalidaException extends DomainException {
    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}

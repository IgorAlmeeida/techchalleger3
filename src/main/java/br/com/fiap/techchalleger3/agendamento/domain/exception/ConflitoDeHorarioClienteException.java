package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class ConflitoDeHorarioClienteException extends DomainException {
    public ConflitoDeHorarioClienteException(String mensagem) {
        super(mensagem);
    }
}

package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando o cliente já tem um agendamento no mesmo horário da nova solicitação.
 */
public class ConflitoDeHorarioClienteException extends DomainException {
    public ConflitoDeHorarioClienteException(String mensagem) {
        super(mensagem);
    }
}

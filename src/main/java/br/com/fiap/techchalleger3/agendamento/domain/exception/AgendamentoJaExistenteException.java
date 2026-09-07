package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class AgendamentoJaExistenteException extends DomainException {
    public AgendamentoJaExistenteException(String mensagem) {
        super(mensagem);
    }
}

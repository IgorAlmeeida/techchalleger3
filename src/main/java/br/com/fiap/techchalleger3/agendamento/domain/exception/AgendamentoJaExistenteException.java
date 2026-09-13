package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando o cliente já possui um agendamento ativo para o mesmo profissional/serviço.
 */
public class AgendamentoJaExistenteException extends DomainException {
    public AgendamentoJaExistenteException(String mensagem) {
        super(mensagem);
    }
}

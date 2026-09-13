package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando a operação solicitada é inválida no estado atual do domínio.
 */
public class OperacaoInvalidaException extends DomainException {
    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}

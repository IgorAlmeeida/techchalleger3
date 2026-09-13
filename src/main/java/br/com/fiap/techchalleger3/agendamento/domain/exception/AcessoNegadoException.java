package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando o usuário não tem permissão para executar a operação solicitada.
 */
public class AcessoNegadoException extends DomainException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}

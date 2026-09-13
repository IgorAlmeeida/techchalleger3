package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando uma entidade buscada por identificador não existe no repositório.
 */
public class RegistroNaoEncontradoException extends DomainException {
    public RegistroNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a) com id: " + id);
    }
}

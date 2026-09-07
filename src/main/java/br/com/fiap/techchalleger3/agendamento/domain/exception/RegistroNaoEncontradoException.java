package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class RegistroNaoEncontradoException extends DomainException {
    public RegistroNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a) com id: " + id);
    }
}

package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class CpfJaCadastradoException extends OperacaoInvalidaException {
    public CpfJaCadastradoException() {
        super("CPF já cadastrado no sistema");
    }
}

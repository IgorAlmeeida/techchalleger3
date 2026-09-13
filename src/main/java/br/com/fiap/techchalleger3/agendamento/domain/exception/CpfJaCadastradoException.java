package br.com.fiap.techchalleger3.agendamento.domain.exception;

@SuppressWarnings("java:S110")
/**
 * Lançada ao tentar cadastrar um CPF que já está associado a outro usuário.
 */
public class CpfJaCadastradoException extends OperacaoInvalidaException {
    public CpfJaCadastradoException() {
        super("CPF já cadastrado no sistema");
    }
}

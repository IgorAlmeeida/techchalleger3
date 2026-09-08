package br.com.fiap.techchalleger3.agendamento.domain.exception;

@SuppressWarnings("java:S110")
public class SenhaFracaException extends OperacaoInvalidaException {
    public SenhaFracaException() {
        super("A senha deve ter no mínimo 8 caracteres, com letras maiúsculas, minúsculas e números");
    }
}

package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class ServicoIndisponivelException extends RuntimeException {
    public ServicoIndisponivelException(String servico) {
        super("Serviço indisponível: " + servico);
    }
}

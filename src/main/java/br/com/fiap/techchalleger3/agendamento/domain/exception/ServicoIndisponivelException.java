package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando o serviço solicitado não está disponível para o profissional/vínculo.
 */
public class ServicoIndisponivelException extends RuntimeException {
    public ServicoIndisponivelException(String servico) {
        super("Serviço indisponível: " + servico);
    }
}

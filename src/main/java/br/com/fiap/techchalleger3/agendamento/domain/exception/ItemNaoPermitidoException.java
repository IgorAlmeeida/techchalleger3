package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada quando se tenta associar um item (serviço, etc.) que não é permitido para o vínculo/agenda alvo.
 */
public class ItemNaoPermitidoException extends DomainException {
    public ItemNaoPermitidoException(Integer servicoId, Integer profissionalVinculoId) {
        super("Serviço " + servicoId + " não está associado ao vínculo " + profissionalVinculoId);
    }
}

package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class ItemNaoPermitidoException extends DomainException {
    public ItemNaoPermitidoException(Integer servicoId, Integer profissionalVinculoId) {
        super("Serviço " + servicoId + " não está associado ao vínculo " + profissionalVinculoId);
    }
}

package br.com.fiap.techchalleger3.agendamento.domain.exception;

public class AgendaEmAbertoException extends DomainException {
    public AgendaEmAbertoException(String contexto) {
        super("Existem agendas futuras em aberto para " + contexto + ". Cancele ou encerre as agendas antes de prosseguir.");
    }
}

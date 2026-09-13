package br.com.fiap.techchalleger3.agendamento.domain.exception;

/**
 * Lançada ao tentar criar uma nova agenda quando já existe uma em aberto para o mesmo profissional/data.
 */
public class AgendaEmAbertoException extends DomainException {
    public AgendaEmAbertoException(String contexto) {
        super("Existem agendas futuras em aberto para " + contexto + ". Cancele ou encerre as agendas antes de prosseguir.");
    }
}

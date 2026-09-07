package br.com.fiap.techchalleger3.agendamento.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionsTest {

    @Test
    void registroNaoEncontrado() {
        var ex = new RegistroNaoEncontradoException("Usuario", 1);
        assertThat(ex.getMessage()).contains("Usuario").contains("1");
    }

    @Test
    void operacaoInvalida() {
        var ex = new OperacaoInvalidaException("msg");
        assertThat(ex.getMessage()).isEqualTo("msg");
    }

    @Test
    void acessoNegado() {
        var ex = new AcessoNegadoException("sem permissao");
        assertThat(ex.getMessage()).contains("sem permissao");
    }

    @Test
    void itemNaoPermitido() {
        var ex = new ItemNaoPermitidoException(5, 10);
        assertThat(ex.getMessage()).contains("5").contains("10");
    }

    @Test
    void conflitoDeEscala() {
        var ex = new ConflitoDeEscalaException("conflito X");
        assertThat(ex.getMessage()).isEqualTo("conflito X");
    }

    @Test
    void agendaEmAberto() {
        var ex = new AgendaEmAbertoException("vinculo 1");
        assertThat(ex.getMessage()).contains("vinculo 1");
    }

    @Test
    void emailJaCadastrado() {
        var ex = new EmailJaCadastradoException("x@x.com");
        assertThat(ex.getMessage()).contains("x@x.com");
    }

    @Test
    void cpfJaCadastrado() {
        var ex = new CpfJaCadastradoException();
        assertThat(ex.getMessage()).contains("CPF");
    }

    @Test
    void senhaFraca() {
        var ex = new SenhaFracaException();
        assertThat(ex.getMessage()).contains("senha");
    }

    @Test
    void servicoIndisponivel() {
        var ex = new ServicoIndisponivelException("Email");
        assertThat(ex.getMessage()).contains("Email");
    }

    @Test
    void credenciaisInvalidas() {
        var ex = new CredenciaisInvalidasException();
        assertThat(ex.getMessage()).contains("inválidos");
    }

    @Test
    void agendamentoJaExistente() {
        var ex = new AgendamentoJaExistenteException("ja existe");
        assertThat(ex.getMessage()).isEqualTo("ja existe");
    }

    @Test
    void conflitoDeHorarioCliente() {
        var ex = new ConflitoDeHorarioClienteException("conflito horario");
        assertThat(ex.getMessage()).isEqualTo("conflito horario");
    }
}

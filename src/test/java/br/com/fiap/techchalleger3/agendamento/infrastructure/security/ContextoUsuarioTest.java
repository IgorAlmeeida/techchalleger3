package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextoUsuarioTest {

    @Test
    void deveArmazenarERecuperarEstabelecimentoId() {
        ContextoUsuario ctx = new ContextoUsuario();
        ctx.setEstabelecimentoId(42);
        assertThat(ctx.getEstabelecimentoId()).isEqualTo(42);
    }

    @Test
    void deveArmazenarERecuperarEmpresaId() {
        ContextoUsuario ctx = new ContextoUsuario();
        ctx.setEmpresaId(99);
        assertThat(ctx.getEmpresaId()).isEqualTo(99);
    }

    @Test
    void deveIniciarComValoresNulos() {
        ContextoUsuario ctx = new ContextoUsuario();
        assertThat(ctx.getEstabelecimentoId()).isNull();
        assertThat(ctx.getEmpresaId()).isNull();
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordAdapterTest {

    private BcryptPasswordAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BcryptPasswordAdapter(new BCryptPasswordEncoder(10));
    }

    @Test
    void encodeEMatches_comSenhaCorreta_retornaTrue() {
        String hash = adapter.encode("Senha@123");

        assertThat(adapter.matches("Senha@123", hash)).isTrue();
    }

    @Test
    void matches_comSenhaErrada_retornaFalse() {
        String hash = adapter.encode("Senha@123");

        assertThat(adapter.matches("SenhaErrada@456", hash)).isFalse();
    }

    @Test
    void encode_geraHashesDiferentes_paraMesmaSenha() {
        String hash1 = adapter.encode("Senha@123");
        String hash2 = adapter.encode("Senha@123");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}

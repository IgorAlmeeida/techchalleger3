package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SenhaTemporariaGeneratorTest {

    @Test
    void gerar_retornaSenhaComDozeCaracteres() {
        String senha = SenhaTemporariaGenerator.gerar();

        assertThat(senha).isNotBlank().hasSize(12);
    }

    @Test
    void gerar_contemMaiuscula_minuscula_digito_eCaractereEspecial() {
        String senha = SenhaTemporariaGenerator.gerar();

        assertThat(senha.chars().anyMatch(Character::isUpperCase)).isTrue();
        assertThat(senha.chars().anyMatch(Character::isLowerCase)).isTrue();
        assertThat(senha.chars().anyMatch(Character::isDigit)).isTrue();
        assertThat(senha.chars().anyMatch(c -> "@#$!".indexOf(c) >= 0)).isTrue();
    }

    @Test
    void gerar_duasChamadas_produzemSenhasDiferentes() {
        String senha1 = SenhaTemporariaGenerator.gerar();
        String senha2 = SenhaTemporariaGenerator.gerar();

        assertThat(senha1).isNotEqualTo(senha2);
    }
}

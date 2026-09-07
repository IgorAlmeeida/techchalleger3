package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import java.security.SecureRandom;

public final class SenhaTemporariaGenerator {

    private static final String MAIUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITOS    = "0123456789";
    private static final String ESPECIAIS  = "@#$!";
    private static final String TODOS      = MAIUSCULAS + MINUSCULAS + DIGITOS + ESPECIAIS;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaTemporariaGenerator() {}

    public static String gerar() {
        StringBuilder sb = new StringBuilder(12);
        sb.append(MAIUSCULAS.charAt(RANDOM.nextInt(MAIUSCULAS.length())));
        sb.append(MINUSCULAS.charAt(RANDOM.nextInt(MINUSCULAS.length())));
        sb.append(DIGITOS.charAt(RANDOM.nextInt(DIGITOS.length())));
        sb.append(ESPECIAIS.charAt(RANDOM.nextInt(ESPECIAIS.length())));
        for (int i = 4; i < 12; i++) {
            sb.append(TODOS.charAt(RANDOM.nextInt(TODOS.length())));
        }
        // embaralha para não deixar padrão fixo nos primeiros caracteres
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }
}

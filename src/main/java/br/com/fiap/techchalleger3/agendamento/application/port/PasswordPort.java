package br.com.fiap.techchalleger3.agendamento.application.port;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

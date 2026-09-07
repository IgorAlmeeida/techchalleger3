package br.com.fiap.techchalleger3.agendamento.application.port;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {
    void put(String chave, Object valor, Duration ttl);
    <T> Optional<T> get(String chave, Class<T> tipo);
    void invalidar(String chave);
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.cache;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisCachePortImpl implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCachePortImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String chave, Object valor, Duration ttl) {
        redisTemplate.opsForValue().set(chave, valor, ttl);
    }

    @Override
    public <T> Optional<T> get(String chave, Class<T> tipo) {
        Object valor = redisTemplate.opsForValue().get(chave);
        if (valor == null) {
            return Optional.empty();
        }
        return Optional.of(tipo.cast(valor));
    }

    @Override
    public void invalidar(String chave) {
        redisTemplate.delete(chave);
    }
}

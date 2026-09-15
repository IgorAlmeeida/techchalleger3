package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import br.com.fiap.techchalleger3.agendamento.application.port.TokenPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey key;
    private final int expiracaoSeg;
    private final int refreshExpiracaoSeg;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-segundos:1800}") int expiracaoSeg,
            @Value("${jwt.refresh-expiration-segundos:604800}") int refreshExpiracaoSeg) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoSeg = expiracaoSeg;
        this.refreshExpiracaoSeg = refreshExpiracaoSeg;
    }

    @Override
    public TokenResponse gerarTokens(Usuario usuario) {
        String accessToken = buildToken(usuario.getUuid(), expiracaoSeg,
                usuario.getEmail(), usuario.getNome(), usuario.getRole().name());
        String refreshToken = gerarRefreshToken(usuario.getUuid());
        return new TokenResponse(accessToken, expiracaoSeg, "Bearer", refreshToken, refreshExpiracaoSeg);
    }

    @Override
    public String gerarRefreshToken(String uuid) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(uuid)
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) refreshExpiracaoSeg * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String validarRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new CredenciaisInvalidasException();
        }
    }

    private String buildToken(String sub, int expiracaoSegundos,
                               String email, String nome, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(sub)
                .claim("email", email)
                .claim("name", nome)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + (long) expiracaoSegundos * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}

package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import br.com.fiap.techchalleger3.agendamento.application.port.TokenPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String SECRET = "minha-chave-de-teste-com-32-caracteres-no-minimo";

    private JwtTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JwtTokenAdapter(SECRET, 1800, 604800);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .uuid("uuid-123")
                .email("teste@teste.com")
                .nome("Fulano")
                .role(RoleEnum.CLIENTE)
                .build();
    }

    @Test
    void gerarTokens_devolveAccessTokenComClaimsCorretas() {
        TokenPort.TokenResponse resposta = adapter.gerarTokens(usuario());

        assertThat(resposta.accessToken()).isNotBlank();
        assertThat(resposta.refreshToken()).isNotBlank();
        assertThat(resposta.tokenType()).isEqualTo("Bearer");
        assertThat(resposta.expiresIn()).isEqualTo(1800);
        assertThat(resposta.refreshExpiresIn()).isEqualTo(604800);

        Claims claims = parseClaims(resposta.accessToken());
        assertThat(claims.getSubject()).isEqualTo("uuid-123");
        assertThat(claims.get("email", String.class)).isEqualTo("teste@teste.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Fulano");
        assertThat(claims.get("role", String.class)).isEqualTo("CLIENTE");
    }

    @Test
    void gerarRefreshToken_temSubjectCorreto() {
        String refresh = adapter.gerarRefreshToken("uuid-456");

        Claims claims = parseClaims(refresh);
        assertThat(claims.getSubject()).isEqualTo("uuid-456");
    }

    @Test
    void validarRefreshToken_comTokenValido_retornaSubject() {
        String refresh = adapter.gerarRefreshToken("uuid-789");

        String subject = adapter.validarRefreshToken(refresh);

        assertThat(subject).isEqualTo("uuid-789");
    }

    @Test
    void validarRefreshToken_comTokenExpirado_lancaExcecao() {
        JwtTokenAdapter adapterExpiraRapido = new JwtTokenAdapter(SECRET, 1800, -1);
        String tokenExpirado = adapterExpiraRapido.gerarRefreshToken("uuid-expirado");

        assertThatThrownBy(() -> adapter.validarRefreshToken(tokenExpirado))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void validarRefreshToken_comAssinaturaDiferente_lancaExcecao() {
        JwtTokenAdapter outroAdapter =
                new JwtTokenAdapter("outra-chave-completamente-diferente-32-chars", 1800, 604800);
        String tokenDeOutraChave = outroAdapter.gerarRefreshToken("uuid-x");

        assertThatThrownBy(() -> adapter.validarRefreshToken(tokenDeOutraChave))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void validarRefreshToken_comStringInvalida_lancaExcecao() {
        assertThatThrownBy(() -> adapter.validarRefreshToken("nao-e-um-jwt"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

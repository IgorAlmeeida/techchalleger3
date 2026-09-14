package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class AppJwtRolesConverterTest {

    private final AppJwtRolesConverter converter = new AppJwtRolesConverter();

    private Jwt jwtComRole(String role) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("sub", "uuid-teste");
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.build();
    }

    @Test
    void convert_comRoleAdmin_retornaAuthorityRoleAdmin() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtComRole("ADMIN"));

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void convert_comRoleCliente_retornaAuthorityRoleCliente() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtComRole("CLIENTE"));

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CLIENTE");
    }

    @Test
    void convert_semClaimRole_retornaListaVazia() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtComRole(null));

        assertThat(authorities).isEmpty();
    }
}

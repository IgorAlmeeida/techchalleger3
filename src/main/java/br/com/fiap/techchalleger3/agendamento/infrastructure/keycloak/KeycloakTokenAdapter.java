package br.com.fiap.techchalleger3.agendamento.infrastructure.keycloak;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ServicoIndisponivelException;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KeycloakTokenAdapter implements KeycloakTokenPort {

    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String serverUrl;

    public KeycloakTokenAdapter(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder().baseUrl(serverUrl).build();
    }

    @Override
    public TokenResponse obterToken(String username, String password) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build()) {

            AccessTokenResponse r = keycloak.tokenManager().getAccessToken();
            return toTokenResponse(r);

        } catch (NotAuthorizedException e) {
            throw new CredenciaisInvalidasException();
        } catch (CredenciaisInvalidasException e) {
            throw e;
        } catch (jakarta.ws.rs.WebApplicationException e) {
            String body = e.getResponse().readEntity(String.class);
            log.error("Keycloak erro ao obter token: {} — body: {}", e.getMessage(), body);
            throw new ServicoIndisponivelException("Keycloak");
        } catch (Exception e) {
            log.error("Keycloak indisponível ao obter token: {}", e.getMessage());
            throw new ServicoIndisponivelException("Keycloak");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TokenResponse renovarToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        try {
            var body = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(java.util.Map.class);

            return new TokenResponse(
                    (String) body.get("access_token"),
                    toInt(body.get("expires_in")),
                    "Bearer",
                    (String) body.get("refresh_token"),
                    toInt(body.get("refresh_expires_in")));

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CredenciaisInvalidasException();
        } catch (Exception e) {
            log.error("Keycloak indisponível ao renovar token: {}", e.getMessage());
            throw new ServicoIndisponivelException("Keycloak");
        }
    }

    private TokenResponse toTokenResponse(AccessTokenResponse r) {
        return new TokenResponse(
                r.getToken(),
                (int) r.getExpiresIn(),
                "Bearer",
                r.getRefreshToken(),
                (int) r.getRefreshExpiresIn());
    }

    private int toInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Long l) return l.intValue();
        if (value instanceof Number n) return n.intValue();
        return 0;
    }
}

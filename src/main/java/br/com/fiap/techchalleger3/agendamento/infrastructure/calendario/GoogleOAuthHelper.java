package br.com.fiap.techchalleger3.agendamento.infrastructure.calendario;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Component
public class GoogleOAuthHelper {

    @Value("${google.calendar.client-id}")
    private String clientId;

    @Value("${google.calendar.client-secret}")
    private String clientSecret;

    @Value("${google.calendar.redirect-uri}")
    private String redirectUri;

    public String gerarUrlAutorizacao(String state) {
        try {
            GoogleAuthorizationCodeFlow flow = buildFlow();
            return flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setState(state)
                    .setAccessType("offline")
                    .set("prompt", "consent")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar URL de autorização Google: " + e.getMessage(), e);
        }
    }

    public TokenResponse trocarCodigoPorTokens(String code) {
        try {
            return new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
            ).execute();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Falha ao trocar código por tokens Google: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private GoogleAuthorizationCodeFlow buildFlow() throws GeneralSecurityException, IOException {
        GoogleClientSecrets secrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        secrets.setWeb(details);

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                secrets,
                Collections.singletonList(CalendarScopes.CALENDAR_EVENTS)
        ).build();
    }
}

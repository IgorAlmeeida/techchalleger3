package br.com.fiap.techchalleger3.agendamento.infrastructure.keycloak;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.EmailJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ServicoIndisponivelException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KeycloakAdminAdapter implements KeycloakAdminPort {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakAdminAdapter(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret) {
        this.realm = realm;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    @Override
    public String criarUsuario(String email, String nome, String senha, String role, boolean temporary) {
        String userId = criarUsuarioNoKeycloak(email, nome, senha, temporary);
        atribuirRole(userId, role);
        return userId;
    }

    private String criarUsuarioNoKeycloak(String email, String nome, String senha, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senha);
        credential.setTemporary(temporary);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(nome);
        user.setLastName("");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(List.of());
        user.setCredentials(List.of(credential));

        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() == 409) throw new EmailJaCadastradoException(email);
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                log.error("Keycloak retornou {} ao criar usuário {}", response.getStatus(), email);
                throw new ServicoIndisponivelException("Keycloak");
            }
            String location = response.getHeaderString("Location");
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (EmailJaCadastradoException | ServicoIndisponivelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao criar usuário no Keycloak: {}", e.getMessage());
            throw new ServicoIndisponivelException("Keycloak");
        }
    }

    @Override
    public void redefinirSenha(String keycloakId, String novaSenha, boolean temporaria) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(novaSenha);
            credential.setTemporary(temporaria);
            keycloak.realm(realm).users().get(keycloakId).resetPassword(credential);
        } catch (Exception e) {
            log.error("Erro ao redefinir senha do usuário {}: {}", keycloakId, e.getMessage());
            throw new ServicoIndisponivelException("Keycloak");
        }
    }

    private void atribuirRole(String userId, String roleName) {
        try {
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(role));
        } catch (Exception e) {
            log.error("Erro ao atribuir role '{}' ao usuário {}: {}", roleName, userId, e.getMessage());
            throw new ServicoIndisponivelException("Keycloak");
        }
    }
}

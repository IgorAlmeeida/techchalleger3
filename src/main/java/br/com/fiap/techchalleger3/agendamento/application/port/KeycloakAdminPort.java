package br.com.fiap.techchalleger3.agendamento.application.port;

public interface KeycloakAdminPort {
    /**
     * Cria usuário no Keycloak e retorna o keycloakId (sub).
     * @param temporary se true, o usuário será obrigado a trocar a senha no primeiro login
     */
    String criarUsuario(String email, String nome, String senha, String role, boolean temporary);

    /**
     * Redefine a senha de um usuário existente no Keycloak.
     * @param keycloakId o sub/id do usuário no Keycloak
     * @param novaSenha a nova senha
     * @param temporaria se true, o usuário será obrigado a trocar a senha no próximo login
     */
    void redefinirSenha(String keycloakId, String novaSenha, boolean temporaria);
}

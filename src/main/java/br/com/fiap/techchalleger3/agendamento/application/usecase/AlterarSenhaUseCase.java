package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlterarSenhaUseCase {

    private final KeycloakTokenPort keycloakTokenPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final UsuarioRepositoryPort usuarioPort;

    public void executar(String keycloakSub, String emailDoToken, String senhaAtual, String senhaNova) {
        try {
            keycloakTokenPort.obterToken(emailDoToken, senhaAtual);
        } catch (CredenciaisInvalidasException e) {
            throw e;
        }

        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

        keycloakAdminPort.redefinirSenha(usuario.getKeycloakId(), senhaNova, false);
    }
}

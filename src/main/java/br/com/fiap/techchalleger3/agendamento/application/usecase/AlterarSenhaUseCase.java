package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlterarSenhaUseCase {

    private final PasswordPort passwordPort;
    private final UsuarioRepositoryPort usuarioPort;

    public void executar(String uuid, String senhaAtual, String senhaNova) {
        Usuario usuario = usuarioPort.buscarPorUuid(uuid)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", uuid));
        if (!passwordPort.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        usuarioPort.atualizarSenha(uuid, passwordPort.encode(senhaNova));
    }
}

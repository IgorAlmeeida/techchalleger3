package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.TokenPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordPort passwordPort;
    private final TokenPort tokenPort;

    public TokenPort.TokenResponse executar(String email, String senha) {
        var usuario = usuarioPort.buscarPorEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordPort.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return tokenPort.gerarTokens(usuario);
    }
}

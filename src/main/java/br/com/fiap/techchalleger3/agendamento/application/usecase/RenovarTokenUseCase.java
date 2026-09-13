package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.TokenPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RenovarTokenUseCase {

    private final TokenPort tokenPort;
    private final UsuarioRepositoryPort usuarioPort;

    public TokenPort.TokenResponse executar(String refreshToken) {
        String uuid = tokenPort.validarRefreshToken(refreshToken);
        Usuario usuario = usuarioPort.buscarPorUuid(uuid)
                .orElseThrow(CredenciaisInvalidasException::new);
        return tokenPort.gerarTokens(usuario);
    }
}

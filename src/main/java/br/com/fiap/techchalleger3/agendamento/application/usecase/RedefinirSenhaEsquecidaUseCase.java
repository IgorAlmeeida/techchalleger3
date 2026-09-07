package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SenhaTemporariaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedefinirSenhaEsquecidaUseCase {

    private final ClienteRepositoryPort clientePort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final EmailSenderPort emailSenderPort;

    public void executar(String email) {
        String nomeUsuario = null;
        String keycloakId = null;

        Optional<Cliente> cliente = clientePort.buscarPorEmail(email);
        if (cliente.isPresent()) {
            nomeUsuario = cliente.get().getNome();
            keycloakId = resolverKeycloakId(cliente.get().getUsuarioId());
        } else {
            Optional<Profissional> profissional = profissionalPort.buscarPorEmail(email);
            if (profissional.isPresent()) {
                nomeUsuario = profissional.get().getNome();
                keycloakId = resolverKeycloakId(profissional.get().getUsuarioId());
            }
        }

        // Sempre responde 200 independente de encontrar ou não (anti-enumeração)
        if (keycloakId == null) {
            log.info("[RedefinirSenha] Email '{}' não encontrado na base — nenhuma ação tomada.", email);
            return;
        }

        String senhaTemp = SenhaTemporariaGenerator.gerar();
        keycloakAdminPort.redefinirSenha(keycloakId, senhaTemp, true);

        emailSenderPort.enviar(new EmailMensagem(
                email,
                "redefinicao-senha",
                Map.of("nome", nomeUsuario, "email", email, "senhaTemp", senhaTemp)
        ));
    }

    private String resolverKeycloakId(Integer usuarioId) {
        return usuarioPort.buscarPorId(usuarioId)
                .map(Usuario::getKeycloakId)
                .orElse(null);
    }
}

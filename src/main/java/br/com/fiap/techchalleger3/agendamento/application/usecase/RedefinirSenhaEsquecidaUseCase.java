package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SenhaTemporariaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedefinirSenhaEsquecidaUseCase {

    private final ClienteRepositoryPort clientePort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordPort passwordPort;
    private final EmailSenderPort emailSenderPort;

    @Transactional
    public void executar(String email) {
        Optional<Usuario> usuarioOpt = usuarioPort.buscarPorEmail(email);

        if (usuarioOpt.isEmpty()) {
            log.info("[RedefinirSenha] Email '{}' não encontrado — nenhuma ação tomada.", email);
            return;
        }

        Usuario usuario = usuarioOpt.get();
        String nomeUsuario = resolverNome(usuario, email);
        String senhaTemp = SenhaTemporariaGenerator.gerar();

        usuarioPort.atualizarSenha(usuario.getUuid(), passwordPort.encode(senhaTemp));

        emailSenderPort.enviar(new EmailMensagem(
                email,
                "redefinicao-senha",
                Map.of("nome", nomeUsuario, "email", email, "senhaTemp", senhaTemp)
        ));
    }

    private String resolverNome(Usuario usuario, String email) {
        Optional<Cliente> cliente = clientePort.buscarPorEmail(email);
        if (cliente.isPresent()) return cliente.get().getNome();

        Optional<Profissional> profissional = profissionalPort.buscarPorEmail(email);
        if (profissional.isPresent()) return profissional.get().getNome();

        return usuario.getNome() != null ? usuario.getNome() : email;
    }
}

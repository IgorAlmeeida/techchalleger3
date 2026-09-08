package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SincronizarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;
    private final ProfissionalRepositoryPort profissionalPort;

    @Transactional
    public Usuario executar(String keycloakSub, RoleEnum role, String nome) {
        Optional<Usuario> existente = usuarioPort.buscarPorCodKeycloak(keycloakSub);
        if (existente.isPresent()) {
            return existente.get();
        }

        Usuario usuario = usuarioPort.salvar(Usuario.builder()
                .keycloakId(keycloakSub)
                .role(role)
                .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                .build());

        String nomeSanitizado = (nome != null && !nome.isBlank()) ? nome : keycloakSub;

        if (RoleEnum.CLIENTE.equals(role)) {
            clientePort.salvar(Cliente.builder()
                    .usuarioId(usuario.getId())
                    .nome(nomeSanitizado)
                    .cpf("")
                    .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                    .build());
        } else if (RoleEnum.PROFISSIONAL.equals(role)) {
            profissionalPort.salvar(Profissional.builder()
                    .usuarioId(usuario.getId())
                    .nome(nomeSanitizado)
                    .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                    .build());
        }

        return usuario;
    }
}

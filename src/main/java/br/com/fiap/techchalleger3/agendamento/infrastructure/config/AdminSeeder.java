package br.com.fiap.techchalleger3.agendamento.infrastructure.config;

import br.com.fiap.techchalleger3.agendamento.application.port.PasswordPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordPort passwordPort;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioPort.buscarPorEmail("admin@agendamento.com").isPresent()) {
            return;
        }
        usuarioPort.salvar(Usuario.builder()
                .uuid(UUID.randomUUID().toString())
                .email("admin@agendamento.com")
                .nome("Administrador")
                .senhaHash(passwordPort.encode("Admin@1234"))
                .role(RoleEnum.ADMIN)
                .build());
        log.info("[AdminSeeder] Usuário admin criado — email: admin@agendamento.com / senha: Admin@1234");
    }
}

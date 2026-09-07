package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SenhaTemporariaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CadastrarProfissionalUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final EmailSenderPort emailSenderPort;

    @Transactional
    public Profissional executar(String nome, String email, List<String> especialidades, String endereco) {
        String senhaTemp = SenhaTemporariaGenerator.gerar();

        String keycloakId = keycloakAdminPort.criarUsuario(email, nome, senhaTemp, "PROFISSIONAL", false);

        Usuario usuario = usuarioPort.salvar(Usuario.builder()
                .keycloakId(keycloakId)
                .role(RoleEnum.PROFISSIONAL)
                .build());

        Profissional profissional = profissionalPort.salvar(Profissional.builder()
                .usuarioId(usuario.getId())
                .nome(nome)
                .email(email)
                .especialidades(especialidades)
                .endereco(endereco)
                .build());

        emailSenderPort.enviar(new EmailMensagem(
                email,
                "cadastro-profissional",
                Map.of("nome", nome, "email", email, "senhaTemp", senhaTemp)
        ));

        return profissional;
    }
}

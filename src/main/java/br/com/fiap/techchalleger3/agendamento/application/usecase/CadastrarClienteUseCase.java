package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CpfJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.SenhaFracaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private static final java.util.regex.Pattern SENHA_PATTERN =
            java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;
    private final KeycloakAdminPort keycloakAdminPort;

    @Transactional
    @SuppressWarnings("java:S107")
    public Cliente executar(String nome, String email, String password, String cpf,
                            LocalDate dataNascimento, String telefone, String sexo, String endereco) {
        if (!SENHA_PATTERN.matcher(password).matches()) {
            throw new SenhaFracaException();
        }
        if (clientePort.existePorCpf(cpf)) {
            throw new CpfJaCadastradoException();
        }

        String keycloakId = keycloakAdminPort.criarUsuario(email, nome, password, "CLIENTE", false);

        Usuario usuario = usuarioPort.salvar(Usuario.builder()
                .keycloakId(keycloakId)
                .role(RoleEnum.CLIENTE)
                .build());

        return clientePort.salvar(Cliente.builder()
                .usuarioId(usuario.getId())
                .nome(nome)
                .email(email)
                .cpf(cpf)
                .dataNascimento(dataNascimento)
                .telefone(telefone)
                .sexo(sexo)
                .endereco(endereco)
                .build());
    }
}

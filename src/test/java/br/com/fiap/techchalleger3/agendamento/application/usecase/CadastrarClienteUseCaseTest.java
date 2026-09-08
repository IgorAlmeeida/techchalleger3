package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakAdminPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.CpfJaCadastradoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.SenhaFracaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarClienteUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private KeycloakAdminPort keycloakAdminPort;
    @InjectMocks private CadastrarClienteUseCase useCase;

    @Test
    void deveCadastrar_quandoDadosValidos() {
        when(clientePort.existePorCpf("123.456.789-00")).thenReturn(false);
        when(keycloakAdminPort.criarUsuario(any(), any(), any(), any(), anyBoolean())).thenReturn("kc-1");
        when(usuarioPort.salvar(any())).thenReturn(Usuario.builder().id(1).build());
        Cliente clienteSalvo = Cliente.builder().id(1).nome("Maria").build();
        when(clientePort.salvar(any())).thenReturn(clienteSalvo);

        Cliente result = useCase.executar("Maria", "maria@test.com", "Senha123A",
                "123.456.789-00", LocalDate.of(1990, 1, 1), "11999", "F", "Rua A");

        assertThat(result.getNome()).isEqualTo("Maria");
    }

    @Test
    void deveLancar_quandoSenhaFraca() {
        assertThatThrownBy(() -> useCase.executar("Maria", "maria@test.com", "fraca",
                "123.456.789-00", null, null, null, null))
                .isInstanceOf(SenhaFracaException.class);
    }

    @Test
    void deveLancar_quandoCpfJaCadastrado() {
        when(clientePort.existePorCpf("123.456.789-00")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("Maria", "maria@test.com", "Senha123A",
                "123.456.789-00", null, null, null, null))
                .isInstanceOf(CpfJaCadastradoException.class);
    }
}

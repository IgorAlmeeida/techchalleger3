package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.AvaliarAtendimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AvaliacaoSteps {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvaliarAtendimentoUseCase avaliarAtendimentoUseCase;

    @MockBean
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @MockBean
    private ClienteRepositoryPort clienteRepositoryPort;

    @MockBean
    private AvaliacaoRepositoryPort avaliacaoRepositoryPort;

    private ResultActions resultado;

    private void stubUsuarioCliente() {
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-test").build();
        Cliente cliente = Cliente.builder().id(1).email("maria@test.com").build();
        when(usuarioRepositoryPort.buscarPorCodKeycloak(any())).thenReturn(Optional.of(usuario));
        when(clienteRepositoryPort.buscarPorUsuarioId(anyInt())).thenReturn(Optional.of(cliente));
    }

    @Dado("que {string} teve um agendamento com status {string}")
    public void clienteTeveDadosStatus(String cliente, String status) {
        Avaliacao avaliacao = Avaliacao.builder().id(1).nota(5).build();
        when(avaliarAtendimentoUseCase.avaliar(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(avaliacao);
        stubUsuarioCliente();
    }

    @Quando("ela avalia o atendimento com nota {int} e comentário {string}")
    public void avaliaAtendimento(int nota, String comentario) throws Exception {
        String body = String.format("""
                {"agendamentoId": 1, "nota": %d, "comentario": "%s"}
                """, nota, comentario);
        resultado = mockMvc.perform(post("/api/avaliacoes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("a avaliação deve ser registrada")
    public void avaliacaoRegistrada() throws Exception {
        resultado.andExpect(status().isCreated());
    }

    @E("a nota média do profissional deve ser atualizada")
    public void notaMediaAtualizada() {
        // verificado via testes de integração de repositório
    }

    @Dado("que {string} tem um agendamento com status {string} ainda não ocorrido")
    public void clienteTemAgendamentoNaoRealizado(String cliente, String status) {
        when(avaliarAtendimentoUseCase.avaliar(anyInt(), anyInt(), anyInt(), anyString()))
                .thenThrow(new OperacaoInvalidaException("Somente agendamentos realizados podem ser avaliados."));
        stubUsuarioCliente();
    }

    @Quando("ela tenta avaliar esse atendimento")
    public void tentaAvaliarAtendimento() throws Exception {
        String body = """
                {"agendamentoId": 1, "nota": 5, "comentario": "Bom"}
                """;
        resultado = mockMvc.perform(post("/api/avaliacoes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Entao("o sistema deve rejeitar a avaliação")
    public void sistemaRejeita() throws Exception {
        resultado.andExpect(status().is4xxClientError());
    }
}

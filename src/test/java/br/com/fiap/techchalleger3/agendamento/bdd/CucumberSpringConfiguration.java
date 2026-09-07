package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.AvaliarAtendimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.BuscarEstabelecimentosUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.EstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ExportarAgendamentoIcsUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarAgendamentosProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarMeusAgendamentosClienteUseCase;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.ContextoEstabelecimentoFilter;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityConfig;
import br.com.fiap.techchalleger3.agendamento.infrastructure.security.SincronizarUsuarioFilter;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.AgendamentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EstabelecimentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller.AgendamentoController;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller.AvaliacaoController;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller.EstabelecimentoController;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@CucumberContextConfiguration
@WebMvcTest({AgendamentoController.class, AvaliacaoController.class, EstabelecimentoController.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    // Security / filters
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SincronizarUsuarioFilter sincronizarUsuarioFilter;
    @MockBean ContextoEstabelecimentoFilter contextoEstabelecimentoFilter;

    // AgendamentoController deps
    @MockBean CriarAgendamentoUseCase criarAgendamentoUseCase;
    @MockBean CancelarAgendamentoUseCase cancelarAgendamentoUseCase;
    @MockBean ListarMeusAgendamentosClienteUseCase listarMeusAgendamentos;
    @MockBean ListarAgendamentosProfissionalUseCase listarAgendamentosProfissional;
    @MockBean ExportarAgendamentoIcsUseCase exportarIcs;
    @MockBean AgendamentoResponseAssembler agendamentoAssembler;

    // AvaliacaoController deps
    @MockBean AvaliarAtendimentoUseCase avaliarAtendimentoUseCase;
    @MockBean AvaliacaoRepositoryPort avaliacaoRepositoryPort;
    @MockBean UsuarioRepositoryPort usuarioRepositoryPort;
    @MockBean ClienteRepositoryPort clienteRepositoryPort;

    // EstabelecimentoController deps
    @MockBean EstabelecimentoUseCase estabelecimentoUseCase;
    @MockBean EstabelecimentoResponseAssembler estabelecimentoAssembler;
    @MockBean BuscarEstabelecimentosUseCase buscarEstabelecimentosUseCase;

    @Before
    public void configureFiltros() throws Exception {
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sincronizarUsuarioFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
        lenient().doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(contextoEstabelecimentoFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }
}

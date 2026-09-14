package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.IntegracaoCalendarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProvedorCalendarioEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.calendario.GoogleOAuthHelper;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.IntegracaoGoogleUrlResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/integracoes/google")
@RequiredArgsConstructor
/**
 * Endpoints para o fluxo OAuth2 de integração com o Google Calendar: geração de URL de
 * autorização, troca do código de autorização por tokens e remoção da integração.
 */
@Tag(name = "Integração Google Calendar", description = "Autorização OAuth2 e sincronização com Google Calendar")
@SecurityRequirement(name = "bearerAuth")
public class IntegracaoGoogleController {

    private final GoogleOAuthHelper oAuthHelper;
    private final IntegracaoCalendarioRepositoryPort integracaoPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;
    private final ProfissionalRepositoryPort profissionalPort;

    @GetMapping("/autorizar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROFISSIONAL')")
    @Operation(summary = "Gera URL de autorização Google Calendar",
            description = "Retorna a URL para o usuário autorizar o acesso ao Google Calendar.")
    public ResponseEntity<IntegracaoGoogleUrlResponse> autorizar(JwtAuthenticationToken principal) {
        String userSub = principal.getToken().getSubject();
        String url = oAuthHelper.gerarUrlAutorizacao(userSub);
        return ResponseEntity.ok(new IntegracaoGoogleUrlResponse(url));
    }

    @GetMapping("/callback")
    @Operation(summary = "Processa callback OAuth2 do Google",
            description = "Endpoint público chamado pelo próprio redirect do Google (não exige Bearer token, "
                    + "pois o navegador não consegue mandar header de autorização num redirect). "
                    + "O parâmetro 'state' (gerado em /autorizar) identifica o usuário que está autorizando.")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) {
        String userSub = state;

        Usuario usuario = usuarioPort.buscarPorUuid(userSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", userSub));

        TokenResponse tokenResponse = oAuthHelper.trocarCodigoPorTokens(code);

        Instant expiraEm = tokenResponse.getExpiresInSeconds() != null
                ? Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds())
                : null;

        IntegracaoCalendarioExterno.IntegracaoCalendarioExternoBuilder builder = IntegracaoCalendarioExterno.builder()
                .provedor(ProvedorCalendarioEnum.GOOGLE)
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiraEm(expiraEm)
                .ativo(true);

        if (RoleEnum.CLIENTE.equals(usuario.getRole())) {
            Integer clienteId = clientePort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente para usuário", usuario.getId()))
                    .getId();
            integracaoPort.desativarPorClienteId(clienteId);
            builder.clienteId(clienteId);
        } else if (RoleEnum.PROFISSIONAL.equals(usuario.getRole())) {
            Integer profissionalId = profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()))
                    .getId();
            integracaoPort.desativarPorProfissionalId(profissionalId);
            builder.profissionalId(profissionalId);
        }

        integracaoPort.salvar(builder.build());
        return ResponseEntity.ok("<html><body><h2>Google Calendar conectado com sucesso!</h2>"
                + "<p>Você já pode fechar esta aba.</p></body></html>");
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROFISSIONAL')")
    @Operation(summary = "Remove integração Google Calendar",
            description = "Desativa a integração ativa do usuário autenticado.")
    public ResponseEntity<Void> remover(JwtAuthenticationToken principal) {
        String userSub = principal.getToken().getSubject();

        Usuario usuario = usuarioPort.buscarPorUuid(userSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", userSub));

        if (RoleEnum.CLIENTE.equals(usuario.getRole())) {
            clientePort.buscarPorUsuarioId(usuario.getId())
                    .ifPresent(c -> integracaoPort.desativarPorClienteId(c.getId()));
        } else if (RoleEnum.PROFISSIONAL.equals(usuario.getRole())) {
            profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .ifPresent(p -> integracaoPort.desativarPorProfissionalId(p.getId()));
        }

        return ResponseEntity.noContent().build();
    }
}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.KeycloakTokenPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.AlterarSenhaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CadastrarClienteUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.LoginUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.RedefinirSenhaEsquecidaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.RenovarTokenUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AlterarSenhaRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CadastrarClienteRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EsqueciSenhaRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.LoginRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.LoginResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.RefreshTokenRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.UsuarioCadastradoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e cadastro de cliente")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RenovarTokenUseCase renovarTokenUseCase;
    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final RedefinirSenhaEsquecidaUseCase redefinirSenhaEsquecidaUseCase;
    private final AlterarSenhaUseCase alterarSenhaUseCase;

    @PostMapping("/login")
    @Operation(summary = "Autentica usuário via Keycloak e retorna JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "503", description = "Keycloak indisponível")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        KeycloakTokenPort.TokenResponse token = loginUseCase.executar(request.username(), request.password());
        return ResponseEntity.ok(toLoginResponse(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renova o access token usando refresh token")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @ApiResponse(responseCode = "503", description = "Keycloak indisponível")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        KeycloakTokenPort.TokenResponse token = renovarTokenUseCase.executar(request.refreshToken());
        return ResponseEntity.ok(toLoginResponse(token));
    }

    private LoginResponse toLoginResponse(KeycloakTokenPort.TokenResponse token) {
        return new LoginResponse(token.accessToken(), token.expiresIn(), token.tokenType(),
                token.refreshToken(), token.refreshExpiresIn());
    }

    @PostMapping("/cadastrar-cliente")
    @Operation(summary = "Auto-cadastro de cliente",
            description = "Cria conta do cliente no Keycloak e no banco local. Sem autenticação prévia.")
    @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    @ApiResponse(responseCode = "422", description = "CPF já cadastrado ou senha fraca")
    @ApiResponse(responseCode = "503", description = "Keycloak indisponível")
    public ResponseEntity<UsuarioCadastradoResponse> cadastrarCliente(
            @Valid @RequestBody CadastrarClienteRequest req) {

        Cliente cliente = cadastrarClienteUseCase.executar(
                req.nome(), req.email(), req.password(), req.cpf(),
                req.dataNascimento(), req.telefone(), req.sexo(), req.endereco());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioCadastradoResponse(cliente.getId(), cliente.getNome(), req.email(), "CLIENTE"));
    }

    @PostMapping("/esqueci-senha")
    @Operation(summary = "Solicita redefinição de senha por email",
            description = "Envia uma senha temporária para o email informado. Responde 200 independente de o email existir (anti-enumeração).")
    @ApiResponse(responseCode = "200", description = "Solicitação processada")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        redefinirSenhaEsquecidaUseCase.executar(request.email());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/alterar-senha")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROFISSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Altera a senha do usuário autenticado",
            description = "Valida a senha atual e define a nova senha. A nova senha não é temporária.")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "401", description = "Senha atual incorreta")
    @ApiResponse(responseCode = "503", description = "Keycloak indisponível")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        String keycloakSub = principal.getToken().getSubject();
        String email = principal.getToken().getClaimAsString("email");
        alterarSenhaUseCase.executar(keycloakSub, email, request.senhaAtual(), request.senhaNova());
        return ResponseEntity.ok().build();
    }
}

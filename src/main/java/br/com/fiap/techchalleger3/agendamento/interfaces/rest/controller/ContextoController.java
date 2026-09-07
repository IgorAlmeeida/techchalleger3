package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarEstabelecimentosContextoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoContextoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contexto")
@RequiredArgsConstructor
@Tag(name = "Contexto", description = "Seleção de estabelecimento pós-login")
@SecurityRequirement(name = "bearerAuth")
public class ContextoController {

    private final ListarEstabelecimentosContextoUseCase useCase;

    @GetMapping("/estabelecimentos")
    @Operation(summary = "Lista estabelecimentos disponíveis para o usuário autenticado",
            description = "CLIENTE e ADMIN recebem todos os estabelecimentos ativos. PROFISSIONAL recebe apenas os estabelecimentos onde tem vínculo ativo.")
    @ApiResponse(responseCode = "200", description = "Lista de estabelecimentos disponíveis")
    public ResponseEntity<List<EstabelecimentoContextoResponse>> listarEstabelecimentos(
            @Parameter(hidden = true) JwtAuthenticationToken principal) {

        String keycloakSub = principal.getToken().getSubject();
        List<Estabelecimento> estabelecimentos = useCase.executar(keycloakSub);

        List<EstabelecimentoContextoResponse> response = estabelecimentos.stream()
                .map(e -> new EstabelecimentoContextoResponse(e.getId(), e.getNome(), e.getCnpj(), e.getEndereco()))
                .toList();

        return ResponseEntity.ok(response);
    }
}

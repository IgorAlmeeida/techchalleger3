package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.usecase.AvaliarAtendimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AvaliacaoResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarAvaliacaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliacoes", description = "Avaliação de atendimentos realizados")
@SecurityRequirement(name = "bearerAuth")
public class AvaliacaoController {

    private final AvaliarAtendimentoUseCase avaliarUseCase;
    private final AvaliacaoRepositoryPort avaliacaoPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Avalia um atendimento concluído",
            description = "O cliente autenticado avalia um agendamento de status CONCLUIDO com nota de 1 a 5.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação registrada"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Agendamento não concluído ou já avaliado")
    })
    public ResponseEntity<AvaliacaoResponse> avaliar(
            @Valid @RequestBody CriarAvaliacaoRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {

        String keycloakSub = principal.getToken().getSubject();
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
        Cliente cliente = clientePort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente", usuario.getId()));

        Avaliacao avaliacao = avaliarUseCase.avaliar(
                request.agendamentoId(), cliente.getId(), request.nota(), request.comentario());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(avaliacao));
    }

    @GetMapping("/estabelecimento/{id}")
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Lista avaliações por estabelecimento")
    @ApiResponse(responseCode = "200", description = "Lista de avaliações")
    public ResponseEntity<List<AvaliacaoResponse>> porEstabelecimento(
            @Parameter(description = "ID do estabelecimento") @PathVariable Integer id) {
        return ResponseEntity.ok(
                avaliacaoPort.listarPorEstabelecimentoId(id).stream().map(this::toResponse).toList());
    }

    @GetMapping("/profissional-vinculo/{id}")
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Lista avaliações por vínculo de profissional")
    @ApiResponse(responseCode = "200", description = "Lista de avaliações")
    public ResponseEntity<List<AvaliacaoResponse>> porProfissionalVinculo(
            @Parameter(description = "ID do vínculo de profissional") @PathVariable Integer id) {
        return ResponseEntity.ok(
                avaliacaoPort.listarPorProfissionalVinculoId(id).stream().map(this::toResponse).toList());
    }

    private AvaliacaoResponse toResponse(Avaliacao a) {
        return new AvaliacaoResponse(a.getId(), a.getAgendamentoId(), a.getClienteId(),
                a.getEstabelecimentoId(), a.getProfissionalVinculoId(), a.getNota(),
                a.getComentario(), a.getDhInsert());
    }
}

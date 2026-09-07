package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.AtualizarEscalaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarEscalaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarEscalasUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EscalaResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AtualizarEscalaRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarEscalaRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EscalaDetalhadaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EscalaResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/escalas")
@RequiredArgsConstructor
@Tag(name = "Escalas", description = "Gerenciamento de escalas recorrentes de disponibilidade dos profissionais")
@SecurityRequirement(name = "bearerAuth")
public class EscalaController {

    private final CriarEscalaUseCase criarEscalaUseCase;
    private final AtualizarEscalaUseCase atualizarEscalaUseCase;
    private final ListarEscalasUseCase listarEscalasUseCase;
    private final EscalaResponseAssembler assembler;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Cria uma escala recorrente para um vínculo de profissional",
            description = "Define os horários de atendimento semanais de um profissional em um estabelecimento, associando serviços.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Escala criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo de profissional ou serviço não encontrado"),
            @ApiResponse(responseCode = "422", description = "Conflito de escala no mesmo dia/horário ou serviço não permitido")
    })
    public ResponseEntity<EscalaResponse> criar(
            @Valid @RequestBody CriarEscalaRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityUtils.isAdmin(principal);
        String keycloakSub = principal.getToken().getSubject();
        Escala escala = criarEscalaUseCase.executar(
                request.profissionalVinculoId(),
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim(),
                request.servicosIds(),
                keycloakSub,
                isAdmin
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResponse(escala));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFISSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma escala existente",
            description = "Atualiza dia da semana, horários e serviços. PROFISSIONAL só pode atualizar as próprias escalas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escala atualizada"),
            @ApiResponse(responseCode = "403", description = "Profissional não autorizado"),
            @ApiResponse(responseCode = "404", description = "Escala não encontrada"),
            @ApiResponse(responseCode = "422", description = "Conflito de escala ou serviço não permitido")
    })
    public ResponseEntity<EscalaResponse> atualizar(
            @Parameter(description = "Identificador da escala") @PathVariable Integer id,
            @Valid @RequestBody AtualizarEscalaRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String keycloakSub = principal.getToken().getSubject();
        Escala escala = atualizarEscalaUseCase.executar(
                id, request.diaSemana(), request.horaInicio(), request.horaFim(),
                request.servicosIds(), keycloakSub, isAdmin);
        return ResponseEntity.ok(assembler.toResponse(escala));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFISSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Lista escalas com serviços detalhados",
            description = "Filtra por estabelecimentoId e/ou profissionalVinculoId. PROFISSIONAL só pode listar as próprias escalas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de escalas"),
            @ApiResponse(responseCode = "400", description = "Nenhum filtro informado"),
            @ApiResponse(responseCode = "403", description = "Profissional não autorizado")
    })
    public ResponseEntity<List<EscalaDetalhadaResponse>> listar(
            @Parameter(description = "Filtrar por id do estabelecimento") @RequestParam(required = false) Integer estabelecimentoId,
            @Parameter(description = "Filtrar por id do vínculo de profissional") @RequestParam(required = false) Integer profissionalVinculoId,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String keycloakSub = principal.getToken().getSubject();
        return ResponseEntity.ok(listarEscalasUseCase.executar(estabelecimentoId, profissionalVinculoId, keycloakSub, isAdmin));
    }
}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.GerarAgendaUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarAgendasUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.AgendaResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendaDetalhadaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.GerarAgendaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendas")
@RequiredArgsConstructor
@Tag(name = "Agendas", description = "Geração e cancelamento de agendas concretas a partir de escalas")
@SecurityRequirement(name = "bearerAuth")
public class AgendaController {

    private final GerarAgendaUseCase gerarAgendaUseCase;
    private final CancelarAgendaUseCase cancelarAgendaUseCase;
    private final ListarAgendasUseCase listarAgendasUseCase;
    private final AgendaResponseAssembler assembler;

    @PostMapping("/gerar")
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Gera agendas concretas a partir de uma escala, para um intervalo de datas",
            description = "Para cada data no intervalo cujo dia da semana coincida com o dia da escala, uma agenda com slots de atendimento é criada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendas geradas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Escala não encontrada"),
            @ApiResponse(responseCode = "422", description = "Intervalo de datas inválido ou agenda já existente no período")
    })
    public ResponseEntity<List<AgendaResponse>> gerar(
            @Valid @RequestBody GerarAgendaRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityUtils.isAdmin(principal);
        String keycloakSub = principal.getToken().getSubject();
        List<Agenda> agendas = gerarAgendaUseCase.executar(
                request.escalaId(), request.dataInicio(), request.dataFim(), keycloakSub, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendas.stream().map(assembler::toResponse).toList());
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Cancela uma agenda inteira em lote",
            description = "Todos os slots AGENDADO da agenda são cancelados com notificação por email.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agenda cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Profissional não é dono desta agenda"),
            @ApiResponse(responseCode = "404", description = "Agenda não encontrada"),
            @ApiResponse(responseCode = "422", description = "Agenda não pode ser cancelada no estado atual")
    })
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "Identificador da agenda") @PathVariable Integer id,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityUtils.isAdmin(principal);
        String keycloakSub = principal.getToken().getSubject();
        cancelarAgendaUseCase.executar(id, keycloakSub, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFISSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Lista agendas com serviços detalhados",
            description = "Filtra por profissionalVinculoId e/ou estabelecimentoId. PROFISSIONAL só pode listar agendas do próprio vínculo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agendas"),
            @ApiResponse(responseCode = "400", description = "Nenhum filtro informado"),
            @ApiResponse(responseCode = "403", description = "Profissional não autorizado")
    })
    public ResponseEntity<Page<AgendaDetalhadaResponse>> listar(
            @Parameter(description = "Filtrar por id do vínculo de profissional") @RequestParam(required = false) Integer profissionalVinculoId,
            @Parameter(description = "Filtrar por id do estabelecimento") @RequestParam(required = false) Integer estabelecimentoId,
            @Parameter(description = "Data de início do intervalo (inclusive)", example = "2026-08-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do intervalo (inclusive)", example = "2026-08-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Número da página (base 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String keycloakSub = principal.getToken().getSubject();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("dataAgenda").ascending());
        Page<AgendaDetalhadaResponse> resultado = listarAgendasUseCase.executar(
                profissionalVinculoId, estabelecimentoId, dataInicio, dataFim, keycloakSub, isAdmin, pageable);
        return ResponseEntity.ok(resultado);
    }
}

package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CancelarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarAgendamentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ExportarAgendamentoIcsUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarAgendamentosProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarMeusAgendamentosClienteUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.AgendamentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendamentoResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarAgendamentoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Criação, cancelamento e consulta de agendamentos")
@SecurityRequirement(name = "bearerAuth")
public class AgendamentoController {

    private final CriarAgendamentoUseCase criarUseCase;
    private final CancelarAgendamentoUseCase cancelarUseCase;
    private final ListarMeusAgendamentosClienteUseCase listarClienteUseCase;
    private final ListarAgendamentosProfissionalUseCase listarProfissionalUseCase;
    private final ExportarAgendamentoIcsUseCase exportarIcsUseCase;
    private final AgendamentoResponseAssembler assembler;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Cria um agendamento para o cliente autenticado",
            description = "Busca o primeiro slot disponível para o profissional/serviço solicitado. "
                    + "Se um agendamentoId específico for informado, reserva aquele slot diretamente. "
                    + "Caso não haja disponibilidade, retorna HTTP 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado"),
            @ApiResponse(responseCode = "409", description = "Cliente já possui agendamento ativo para este serviço/profissional"),
            @ApiResponse(responseCode = "422", description = "Sem horários disponíveis ou conflito de horário")
    })
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody CriarAgendamentoRequest request,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        String keycloakSub = principal.getToken().getSubject();
        Agendamento agendamento = criarUseCase.executar(
                keycloakSub,
                request.profissionalVinculoId(),
                request.servicoId(),
                request.dataPreferencia(),
                request.agendamentoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResponse(agendamento));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Cancela um agendamento",
            description = "CLIENTE só pode cancelar próprio agendamento. PROFISSIONAL cancela agendamentos do próprio vínculo. ADMIN cancela qualquer agendamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento cancelado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para cancelar este agendamento"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Agendamento não pode ser cancelado no estado atual")
    })
    public ResponseEntity<AgendamentoResponse> cancelar(
            @Parameter(description = "ID do agendamento pai") @PathVariable Integer id,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        String keycloakSub = principal.getToken().getSubject();
        Agendamento agendamento = cancelarUseCase.executar(id, keycloakSub);
        return ResponseEntity.ok(assembler.toResponse(agendamento));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Lista os agendamentos do cliente autenticado",
            description = "Retorna agendamentos pais do cliente. Por padrão lista AGENDADO e RESERVADO.")
    @ApiResponse(responseCode = "200", description = "Lista de agendamentos")
    public ResponseEntity<List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido>> listarMeus(
            @Parameter(description = "Filtrar por status (múltiplos aceitos)") @RequestParam(required = false) List<StatusAgendamentoEnum> status,
            @Parameter(description = "Data de início (inclusive)", example = "2026-08-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim (inclusive)", example = "2026-08-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        String keycloakSub = principal.getToken().getSubject();
        return ResponseEntity.ok(listarClienteUseCase.executar(keycloakSub, status, dataInicio, dataFim));
    }

    @GetMapping("/profissional")
    @PreAuthorize("hasAnyRole('PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Lista agendamentos do profissional autenticado (ou filtrado por vínculo para ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de agendamentos")
    public ResponseEntity<List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido>> listarProfissional(
            @Parameter(description = "Filtrar por vínculo específico") @RequestParam(required = false) Integer profissionalVinculoId,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) List<StatusAgendamentoEnum> status,
            @Parameter(description = "Data de início", example = "2026-08-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim", example = "2026-08-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = br.com.fiap.techchalleger3.agendamento.infrastructure.security.SecurityUtils.isAdmin(principal);
        String keycloakSub = principal.getToken().getSubject();
        return ResponseEntity.ok(listarProfissionalUseCase.executar(
                keycloakSub, profissionalVinculoId, status, dataInicio, dataFim, isAdmin));
    }

    @GetMapping("/{id}/ics")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROFISSIONAL', 'ADMIN')")
    @Operation(summary = "Exporta agendamento como arquivo ICS (iCalendar)",
            description = "Retorna o arquivo .ics para importação em agendas como Google Calendar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo ICS gerado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<byte[]> exportarIcs(
            @Parameter(description = "ID do agendamento") @PathVariable Integer id) {
        byte[] ics = exportarIcsUseCase.exportar(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("agendamento-" + id + ".ics").build());
        return ResponseEntity.ok().headers(headers).body(ics);
    }
}

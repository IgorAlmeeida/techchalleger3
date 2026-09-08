package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.AssociarServicoAoVinculoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.CriarVinculoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.DesvincularProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.DesvincularItemUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarServicosDoVinculoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarOfertaDoEstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ListarVinculosUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.ProfissionalVinculoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AssociarItemRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarVinculoRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalDisponivelResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoItemDetalhadaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoItemResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profissional-vinculos")
@RequiredArgsConstructor
@Tag(name = "Vínculos de Profissional", description = "Associação de profissionais a estabelecimentos e serviços")
@SecurityRequirement(name = "bearerAuth")
public class ProfissionalVinculoController {

    private final CriarVinculoUseCase criarVinculoUseCase;
    private final AssociarServicoAoVinculoUseCase associarServicoUseCase;
    private final ListarVinculosUseCase listarVinculosUseCase;
    private final ListarServicosDoVinculoUseCase listarServicosDoVinculoUseCase;
    private final ListarOfertaDoEstabelecimentoUseCase listarOfertaDoEstabelecimentoUseCase;
    private final DesvincularItemUseCase desvincularItemUseCase;
    private final DesvincularProfissionalUseCase desvincularProfissionalUseCase;
    private final ProfissionalVinculoResponseAssembler assembler;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Associa um profissional a um estabelecimento (admin)",
            description = "Cria um vínculo ativo entre profissional e estabelecimento. Não pode haver vínculo ativo duplicado para o mesmo par.")
    @ApiResponse(responseCode = "201", description = "Vínculo criado com sucesso")
    @ApiResponse(responseCode = "404", description = "Profissional ou estabelecimento não encontrado")
    @ApiResponse(responseCode = "422", description = "Vínculo ativo já existe para este par profissional/estabelecimento")
    public ResponseEntity<VinculoResponse> criar(@Valid @RequestBody CriarVinculoRequest request) {
        ProfissionalVinculo vinculo = criarVinculoUseCase.executar(
                request.profissionalId(), request.estabelecimentoId(), request.dataInicio());
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toVinculoResponse(vinculo));
    }

    @PostMapping("/{id}/itens")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Associa um serviço a um vínculo (admin)",
            description = "Define quais serviços o profissional oferece naquele estabelecimento. Serviço duplicado no mesmo vínculo é rejeitado.")
    @ApiResponse(responseCode = "201", description = "Serviço associado com sucesso")
    @ApiResponse(responseCode = "404", description = "Vínculo ou serviço não encontrado")
    @ApiResponse(responseCode = "422", description = "Serviço já associado a este vínculo")
    public ResponseEntity<VinculoItemResponse> associarServico(
            @Parameter(description = "Identificador do vínculo de profissional") @PathVariable Integer id,
            @Valid @RequestBody AssociarItemRequest request) {
        ProfissionalVinculoServico item = associarServicoUseCase.executar(id, request.servicoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toVinculoItemResponse(item));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista vínculos profissional↔estabelecimento (admin)",
            description = "Filtra por profissionalId e/ou estabelecimentoId. Inclui vínculos ativos e encerrados.")
    @ApiResponse(responseCode = "200", description = "Lista de vínculos")
    public ResponseEntity<Page<VinculoResponse>> listar(
            @Parameter(description = "Filtrar por id do profissional") @RequestParam(required = false) Integer profissionalId,
            @Parameter(description = "Filtrar por id do estabelecimento") @RequestParam(required = false) Integer estabelecimentoId,
            @Parameter(description = "Número da página (base 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size) {
        Page<ProfissionalVinculo> resultado = listarVinculosUseCase.executar(
                profissionalId, estabelecimentoId, PageRequest.of(page, size));
        return ResponseEntity.ok(resultado.map(assembler::toVinculoResponse));
    }

    @GetMapping("/{id}/itens")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista serviços de um vínculo (admin)",
            description = "Retorna os serviços associados a um vínculo, com nome e duração.")
    @ApiResponse(responseCode = "200", description = "Lista de serviços do vínculo")
    @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    public ResponseEntity<List<VinculoItemDetalhadaResponse>> listarItens(
            @Parameter(description = "Identificador do vínculo de profissional") @PathVariable Integer id) {
        return ResponseEntity.ok(listarServicosDoVinculoUseCase.executar(id));
    }

    @GetMapping("/oferta")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Lista profissionais e serviços disponíveis em um estabelecimento (cliente)",
            description = "Retorna todos os vínculos ativos de um estabelecimento com seus serviços.")
    @ApiResponse(responseCode = "200", description = "Lista de profissionais disponíveis no estabelecimento")
    @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
    public ResponseEntity<List<ProfissionalDisponivelResponse>> listarOferta(
            @Parameter(description = "Id do estabelecimento", required = true) @RequestParam Integer estabelecimentoId,
            @Parameter(description = "Filtrar por serviço (opcional)") @RequestParam(required = false) Integer servicoId) {
        return ResponseEntity.ok(listarOfertaDoEstabelecimentoUseCase.executar(estabelecimentoId, servicoId));
    }

    @DeleteMapping("/{vinculoId}/itens/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove serviço de um vínculo (admin)",
            description = "Bloqueia se houver agendas futuras com este serviço no vínculo.")
    @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Vínculo ou serviço não encontrado")
    @ApiResponse(responseCode = "422", description = "Agendas futuras em aberto impedem a remoção")
    public ResponseEntity<Void> desvincularServico(
            @Parameter(description = "Identificador do vínculo de profissional") @PathVariable Integer vinculoId,
            @Parameter(description = "Identificador da associação vínculo/serviço") @PathVariable Integer itemId) {
        desvincularItemUseCase.executar(vinculoId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{vinculoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Encerra vínculo profissional↔estabelecimento (admin)",
            description = "Seta data_fim = hoje. Bloqueia se houver agendas futuras no vínculo.")
    @ApiResponse(responseCode = "204", description = "Vínculo encerrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    @ApiResponse(responseCode = "422", description = "Agendas futuras em aberto impedem o encerramento")
    public ResponseEntity<Void> desvincularProfissional(
            @Parameter(description = "Identificador do vínculo de profissional") @PathVariable Integer vinculoId) {
        desvincularProfissionalUseCase.executar(vinculoId);
        return ResponseEntity.noContent().build();
    }
}

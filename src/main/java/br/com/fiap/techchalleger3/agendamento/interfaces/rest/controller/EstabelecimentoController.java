package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.BuscarEstabelecimentosUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.EstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EstabelecimentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AtualizarEstabelecimentoRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarEstabelecimentoRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estabelecimentos")
@RequiredArgsConstructor
@Tag(name = "Estabelecimentos", description = "CRUD de estabelecimentos e busca pública")
@SecurityRequirement(name = "bearerAuth")
public class EstabelecimentoController {

    private final EstabelecimentoUseCase useCase;
    private final EstabelecimentoResponseAssembler assembler;
    private final BuscarEstabelecimentosUseCase buscarEstabelecimentosUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estabelecimento criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<EstabelecimentoResponse> criar(@Valid @RequestBody CriarEstabelecimentoRequest req) {
        Estabelecimento estabelecimento = useCase.criar(req.nome(), req.cnpj(), req.endereco(),
                req.telefone(), req.responsavelNome(), req.responsavelCpf(), req.fotosUrls());
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResponse(estabelecimento));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista estabelecimentos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de estabelecimentos ativos")
    })
    public ResponseEntity<Page<EstabelecimentoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(useCase.listar(pageable).map(assembler::toResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca estabelecimento por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estabelecimento encontrado"),
            @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
    })
    public ResponseEntity<EstabelecimentoResponse> buscar(
            @Parameter(description = "Identificador do estabelecimento") @PathVariable Integer id) {
        return ResponseEntity.ok(assembler.toResponse(useCase.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estabelecimento atualizado"),
            @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado"),
            @ApiResponse(responseCode = "422", description = "CNPJ já cadastrado por outro estabelecimento")
    })
    public ResponseEntity<EstabelecimentoResponse> atualizar(
            @Parameter(description = "Identificador do estabelecimento") @PathVariable Integer id,
            @Valid @RequestBody AtualizarEstabelecimentoRequest req) {
        Estabelecimento estabelecimento = useCase.atualizar(id, req.nome(), req.cnpj(), req.endereco(),
                req.telefone(), req.responsavelNome(), req.responsavelCpf(), req.fotosUrls());
        return ResponseEntity.ok(assembler.toResponse(estabelecimento));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativa estabelecimento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estabelecimento inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Estabelecimento não encontrado")
    })
    public ResponseEntity<Void> inativar(
            @Parameter(description = "Identificador do estabelecimento") @PathVariable Integer id) {
        useCase.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca estabelecimentos com filtros",
            description = "Busca pública por nome, localização, preço e nota mínima.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de estabelecimentos encontrados")
    })
    public ResponseEntity<List<EstabelecimentoResponse>> buscarComFiltros(
            @Parameter(description = "Nome do estabelecimento (busca parcial)") @RequestParam(required = false) String nome,
            @Parameter(description = "Localização") @RequestParam(required = false) String localizacao,
            @Parameter(description = "Preço mínimo") @RequestParam(required = false) BigDecimal precoMin,
            @Parameter(description = "Preço máximo") @RequestParam(required = false) BigDecimal precoMax,
            @Parameter(description = "Nota mínima (0.0 a 5.0)") @RequestParam(required = false) Double notaMinima,
            @Parameter(description = "Data para verificar disponibilidade") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Número da página (base 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size) {
        Page<Estabelecimento> resultado = buscarEstabelecimentosUseCase.buscar(
                nome, localizacao, null, precoMin, precoMax, notaMinima, data, PageRequest.of(page, size));
        return ResponseEntity.ok(resultado.getContent().stream().map(assembler::toResponse).toList());
    }
}

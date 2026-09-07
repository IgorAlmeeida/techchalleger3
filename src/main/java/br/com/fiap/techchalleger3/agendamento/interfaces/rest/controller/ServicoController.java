package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.ServicoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AtualizarServicoRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CriarServicoRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Serviços", description = "CRUD de serviços oferecidos (admin)")
@SecurityRequirement(name = "bearerAuth")
public class ServicoController {

    private final ServicoUseCase useCase;

    @PostMapping
    @Operation(summary = "Cria serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso")
    })
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody CriarServicoRequest req) {
        Servico servico = useCase.criar(req.nome(), req.duracaoMinutos(), req.preco());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(servico));
    }

    @GetMapping
    @Operation(summary = "Lista serviços ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de serviços ativos")
    })
    public ResponseEntity<Page<ServicoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(useCase.listar(pageable).map(this::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca serviço por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServicoResponse> buscar(
            @Parameter(description = "Identificador do serviço") @PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(useCase.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServicoResponse> atualizar(
            @Parameter(description = "Identificador do serviço") @PathVariable Integer id,
            @Valid @RequestBody AtualizarServicoRequest req) {
        Servico servico = useCase.atualizar(id, req.nome(), req.duracaoMinutos(), req.preco());
        return ResponseEntity.ok(toResponse(servico));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<Void> inativar(
            @Parameter(description = "Identificador do serviço") @PathVariable Integer id) {
        useCase.inativar(id);
        return ResponseEntity.noContent().build();
    }

    private ServicoResponse toResponse(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDuracaoMinutos(), s.getPreco(),
                s.getAtivo(), s.getDhInsert(), s.getDhAtualizacao());
    }
}

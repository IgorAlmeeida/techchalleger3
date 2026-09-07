package br.com.fiap.techchalleger3.agendamento.interfaces.rest.controller;

import br.com.fiap.techchalleger3.agendamento.application.usecase.CadastrarProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.ProfissionalUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AtualizarProfissionalRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.CadastrarProfissionalRequest;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.UsuarioCadastradoResponse;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profissionais")
@RequiredArgsConstructor
@Tag(name = "Admin — Profissionais", description = "CRUD de profissionais")
@SecurityRequirement(name = "bearerAuth")
public class AdminProfissionalController {

    private final CadastrarProfissionalUseCase cadastrarUseCase;
    private final ProfissionalUseCase profissionalUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra profissional",
            description = "Cria conta no Keycloak com senha temporária e envia email de primeiro acesso.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profissional cadastrado e email enviado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "503", description = "Keycloak indisponível")
    })
    public ResponseEntity<UsuarioCadastradoResponse> cadastrar(@Valid @RequestBody CadastrarProfissionalRequest req) {
        Profissional profissional = cadastrarUseCase.executar(
                req.nome(), req.email(), req.especialidades(), req.endereco());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioCadastradoResponse(profissional.getId(), profissional.getNome(), req.email(), "PROFISSIONAL"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista profissionais",
            description = "Retorna profissionais paginados. Filtros opcionais por nome e especialidade. " +
                    "Por padrão lista apenas ativos; use incluirInativos=true para ver todos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de profissionais")
    })
    public ResponseEntity<Page<ProfissionalResponse>> listar(
            Pageable pageable,
            @Parameter(description = "Filtrar por nome (busca parcial)") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtrar por especialidade") @RequestParam(required = false) String especialidade,
            @Parameter(description = "Incluir profissionais inativos") @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(profissionalUseCase.listar(nome, especialidade, incluirInativos, pageable)
                .map(this::toResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFISSIONAL')")
    @Operation(summary = "Busca profissional por id",
            description = "ADMIN pode consultar qualquer profissional. PROFISSIONAL só pode consultar o próprio perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissional encontrado"),
            @ApiResponse(responseCode = "403", description = "Profissional tentando acessar outro perfil"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    public ResponseEntity<ProfissionalResponse> buscar(
            @Parameter(description = "Identificador do profissional") @PathVariable Integer id,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String keycloakSub = principal.getToken().getSubject();
        return ResponseEntity.ok(toResponse(profissionalUseCase.buscarPorId(id, keycloakSub, isAdmin)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFISSIONAL')")
    @Operation(summary = "Atualiza profissional",
            description = "Atualiza nome, especialidades e endereço. ADMIN pode atualizar qualquer profissional; PROFISSIONAL só o próprio perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profissional atualizado"),
            @ApiResponse(responseCode = "403", description = "Profissional tentando atualizar outro perfil"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado")
    })
    public ResponseEntity<ProfissionalResponse> atualizar(
            @Parameter(description = "Identificador do profissional") @PathVariable Integer id,
            @Valid @RequestBody AtualizarProfissionalRequest req,
            @Parameter(hidden = true) JwtAuthenticationToken principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String keycloakSub = principal.getToken().getSubject();
        Profissional profissional = profissionalUseCase.atualizar(
                id, req.nome(), req.especialidades(), req.endereco(), keycloakSub, isAdmin);
        return ResponseEntity.ok(toResponse(profissional));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativa profissional (não remove o registro)",
            description = "Inativação lógica. Bloqueada se o profissional tiver agendas futuras — cancele as agendas primeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profissional inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "422", description = "Profissional possui agendas futuras")
    })
    public ResponseEntity<Void> inativar(
            @Parameter(description = "Identificador do profissional") @PathVariable Integer id) {
        profissionalUseCase.inativar(id);
        return ResponseEntity.noContent().build();
    }

    private ProfissionalResponse toResponse(Profissional p) {
        return new ProfissionalResponse(p.getId(), p.getNome(), p.getEspecialidades(),
                p.getEndereco(), p.getAtivo(), p.getDhInsert(), p.getDhAtualizacao());
    }
}

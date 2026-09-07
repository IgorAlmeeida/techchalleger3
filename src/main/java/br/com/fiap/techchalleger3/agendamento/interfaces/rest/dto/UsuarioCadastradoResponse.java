package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmação de cadastro de usuário")
public record UsuarioCadastradoResponse(
        @Schema(description = "Identificador interno do usuário", example = "1")
        Integer id,

        @Schema(description = "Nome completo do usuário cadastrado", example = "João da Silva")
        String nome,

        @Schema(description = "E-mail do usuário cadastrado", example = "joao.silva@email.com")
        String email,

        @Schema(description = "Papel (role) atribuído ao usuário no sistema", example = "CLIENTE",
                allowableValues = {"CLIENTE", "PROFISSIONAL", "ADMIN"})
        String role
) {}

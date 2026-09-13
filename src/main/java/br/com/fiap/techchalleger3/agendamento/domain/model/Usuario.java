package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Usuário do sistema. Pode ser ADMIN, PROFISSIONAL ou CLIENTE conforme o role.
 */
public class Usuario {
    private Integer id;
    private String uuid;
    private String email;
    private String nome;
    private String senhaHash;
    private RoleEnum role;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}

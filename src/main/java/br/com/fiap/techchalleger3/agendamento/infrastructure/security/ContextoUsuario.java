package br.com.fiap.techchalleger3.agendamento.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Setter
@Component
@RequestScope
public class ContextoUsuario {
    private Integer estabelecimentoId;
    private Integer empresaId;
}

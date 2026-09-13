package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retorna os estabelecimentos visíveis para o usuário autenticado de acordo com seu perfil:
 * profissionais veem apenas estabelecimentos com vínculo ativo; clientes e admins veem todos.
 */
@Service
@RequiredArgsConstructor
public class ListarEstabelecimentosContextoUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ProfissionalVinculoRepositoryPort vinculoPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;

    public List<Estabelecimento> executar(String userSub) {
        Usuario usuario = usuarioPort.buscarPorUuid(userSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário", userSub));

        if (RoleEnum.PROFISSIONAL.equals(usuario.getRole())) {
            var profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", usuario.getId()));
            List<Integer> estabelecimentoIds = vinculoPort.listarEstabelecimentoIdsAtivosPorProfissional(profissional.getId());
            if (estabelecimentoIds.isEmpty()) return List.of();
            return estabelecimentoPort.listarPorIds(estabelecimentoIds);
        }

        // CLIENTE e ADMIN: todos os estabelecimentos ativos
        return estabelecimentoPort.listarAtivos();
    }
}

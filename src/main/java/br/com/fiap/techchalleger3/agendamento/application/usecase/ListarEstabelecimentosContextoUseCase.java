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

@Service
@RequiredArgsConstructor
public class ListarEstabelecimentosContextoUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ProfissionalVinculoRepositoryPort vinculoPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;

    public List<Estabelecimento> executar(String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário", keycloakSub));

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
